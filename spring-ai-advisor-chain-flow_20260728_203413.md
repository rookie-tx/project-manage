# Spring AI Advisor 链调用流程 —— 会话消息如何被添加

> 分析对象:`CoordinatorAgent.execute()` → `chatClient.prompt()...call().content()`
> 依据:实际源码 `spring-ai-client-chat-2.0.0-sources.jar`(Spring AI 2.0.0),非记忆推断。

## 1. 结论速览

- 会话消息的读写**不在** `CoordinatorAgent.execute()` 里。`execute` 只负责发起请求,并通过 `.advisors(a -> a.param(CONVERSATION_ID, "default"))` 把会话 ID 传进 context。
- 真正的读写在 `MessageChatMemoryAdvisor`(在 `CoordinatorAgent.java:39` 通过 `defaultAdvisors(...)` 注册):
  - **用户消息**在 `before()` 写入(同时把历史消息注入到本次 prompt)。
  - **助手回复**在 `after()` 写入。
- advisor 链是一个**递归的洋葱模型(around 环绕)**:`before` 按顺序进入,`after` 逆序返回(LIFO)。
- 链尾由框架自动追加的 `ChatModelCallAdvisor` 真正调用 LLM,它是递归的终点。

## 2. 关键类与职责

| 类 / 接口 | 位置 | 职责 |
| --- | --- | --- |
| `CoordinatorAgent` | 项目内 `agent/CoordinatorAgent.java` | 注册 advisor、发起 `.call()` |
| `DefaultChatClient` | `client/DefaultChatClient.java` | 构建 advisor 链、启动链 |
| `DefaultAroundAdvisorChain` | `client/advisor/DefaultAroundAdvisorChain.java` | 链的执行引擎(`nextCall` 逐个 pop advisor) |
| `BaseAdvisor` | `client/advisor/api/BaseAdvisor.java` | 定义 `adviseCall = before + nextCall + after` 的环绕骨架 |
| `MessageChatMemoryAdvisor` | `client/advisor/MessageChatMemoryAdvisor.java` | 在 before/after 读写 `ChatMemory` |
| `ToolCallingAdvisor` | `client/advisor/ToolCallingAdvisor.java` | 工具调用循环(do-while),由 `defaultTools` 激活 |
| `ChatModelCallAdvisor` | `client/advisor/ChatModelCallAdvisor.java` | 链尾终点,真正 `chatModel.call()` 打 LLM |
| `AnthropicChatModel` | `spring-ai-anthropic` | `call → internalCall`,单次 HTTP,不再内部循环工具 |

## 3. 完整调用链

```
CoordinatorAgent.execute()                         CoordinatorAgent.java:57
   .call().content()
      │
      ▼
DefaultCallResponseSpec.doGetObservableChatClientResponse()
      advisorChain.nextCall(request)               DefaultChatClient.java:658   ← 启动链
      │
      ▼
DefaultAroundAdvisorChain.nextCall()               DefaultAroundAdvisorChain.java:97
      advisor = callAdvisors.pop();                (从 deque 弹出下一个 advisor)
      advisor.adviseCall(request, this)            (传入链自身 this,以便递归)
      │
      ▼
MessageChatMemoryAdvisor(via BaseAdvisor.adviseCall) BaseAdvisor.java:47  ★ 环绕核心
      ┌─ before(request, chain)                    ← 写入用户消息、注入历史
      ├─ chain.nextCall(processedRequest)          ← 递归下一环(回到 nextCall)
      │       │
      │       ▼
      │  ToolCallingAdvisor.adviseCall()           ToolCallingAdvisor.java:120  工具循环
      │       do {
      │         chain.copy(this).nextCall(req)      ← 穿到链尾打一次 LLM
      │         if (hasToolCalls) executeToolCalls()← 执行工具(DelegateTools)
      │       } while (还有 tool_use);              ← 多轮循环,直到无工具调用
      │            │
      │            ▼
      │       ChatModelCallAdvisor.adviseCall()     ChatModelCallAdvisor.java:53  链尾终点
      │            chatModel.call(prompt)           ChatModelCallAdvisor.java:58  ← 真正调用 LLM
      │            return(不再 nextCall)            AnthropicChatModel:531 单次 HTTP,不循环
      └─ after(response, chain)                    ← 助手回复写入 memory(整个工具循环外只写一次)
```

> 注:memory(order +200)在工具循环(+300)**外层**,所以多轮工具调用对 memory 只算"一次 nextCall"。详见第 8 节。

## 4. 环绕(around)机制:after 到底在哪被调用

`MessageChatMemoryAdvisor` 自身没有 `adviseCall`,它实现的是 `BaseChatMemoryAdvisor`,后者继承 `BaseAdvisor`。before/after 的调度骨架来自 `BaseAdvisor` 的 default 方法:

```java
// BaseAdvisor.java:47-54
default ChatClientResponse adviseCall(ChatClientRequest req, CallAdvisorChain chain) {
    ChatClientRequest processed = before(req, chain);      // 51: 前置——写用户消息 / 注入历史
    ChatClientResponse resp     = chain.nextCall(processed);// 52: 递归调用链的下一环
    return after(resp, chain);                             // 53: ★ after 在这里被调用
}
```

这不是"先跑完所有 before,再跑完所有 after"的两趟遍历,而是递归栈:

1. `nextCall` 从 deque **pop** 出一个 advisor,调它的 `adviseCall`。
2. `adviseCall` 先执行本层 `before`,再调 `chain.nextCall(...)` —— 回到步骤 1,pop 下一个 advisor。
3. 递归到链尾 `ChatModelCallAdvisor`,它请求 LLM 并返回,**不再** `nextCall`(递归终止)。
4. 回溯:每层 `nextCall` 返回后,控制权回到该层第 53 行,执行本层 `after`。

因此 `after` 的执行顺序与 `before` **正好相反(LIFO)**。对本例:

```
before:  [其他 advisor].before → MessageChatMemoryAdvisor.before → (链尾 LLM 调用)
after :  (链尾 LLM 调用) → MessageChatMemoryAdvisor.after → [其他 advisor].after
```

## 5. MessageChatMemoryAdvisor 的读写细节

**before()** —— 调 LLM 之前:
1. 从 context 取 `conversationId`(本例为 `"default"`)。
2. `chatMemory.get(conversationId)` 读出历史消息。
3. 把历史消息 + 当前 prompt 指令合并,并把 `SystemMessage` 挪到最前。
4. 取出最后一条 user/tool 消息,`chatMemory.add(conversationId, 用户消息)` —— **用户输入在此写入**。
5. 返回带历史的增强 request 给链的下一环。

**after()** —— LLM 返回之后:
1. 从 `ChatResponse.getResults()` 提取每条结果的 assistant 消息。
2. `chatMemory.add(conversationId, assistantMessages)` —— **模型回复在此写入**。

底层存储由构造器注入的 `ChatMemory` bean 决定(来自 `spring-ai-starter-model-chat-memory`,默认 `MessageWindowChatMemory` + 内存 / JDBC repository)。

## 6. 链的构建与终点

```java
// DefaultChatClient.java:1189-1202
private BaseAdvisorChain buildAdvisorChain() {
    List<Advisor> chain = new ArrayList<>(this.advisors);          // 用户注册的 advisor
    chain.add(ChatModelCallAdvisor.builder().chatModel(...).build());   // 链尾兜底:真正打 LLM
    chain.add(ChatModelStreamAdvisor.builder().chatModel(...).build()); // 流式对应终点
    return DefaultAroundAdvisorChain.builder(...).pushAll(chain).build();
}
```

`ChatModelCallAdvisor` 直接实现 `CallAdvisor`(不是 `BaseAdvisor`),其 `adviseCall` 只做一件事:`chatModel.call(prompt)` 后直接返回,不再 `nextCall` —— 这就是递归停止的地方(`ChatModelCallAdvisor.java:53-64`)。

## 7. 链尾 ChatModelCallAdvisor 展开:真正的 chatModel.call

`ChatModelCallAdvisor.adviseCall()`(`ChatModelCallAdvisor.java:53-64`)完整逻辑:

```java
public ChatClientResponse adviseCall(ChatClientRequest req, CallAdvisorChain chain) {
    // 1. 如果开启了结构化输出,把 format/schema 指令追加到 user message
    ChatClientRequest formatted = augmentWithFormatInstructions(req);   // 56
    // 2. 真正打 LLM —— 单次 HTTP 请求,不循环、不 nextCall
    ChatResponse chatResponse = this.chatModel.call(formatted.prompt()); // 58
    // 3. 包装成 ChatClientResponse 返回,开始逐层回溯
    return ChatClientResponse.builder()
        .chatResponse(chatResponse)
        .context(Map.copyOf(formatted.context()))
        .build();                                                        // 60-63
}
```

要点:
- 它**不实现** `BaseAdvisor`,所以没有 before/after 环绕语义,是链的纯终点。
- `getOrder()` 返回 `Ordered.LOWEST_PRECEDENCE`(`ChatModelCallAdvisor.java:110`),排序后永远在最内层。
- `augmentWithFormatInstructions`:仅当 context 里有 `OUTPUT_FORMAT` / `STRUCTURED_OUTPUT_SCHEMA` 时才改写 prompt(本例未用结构化输出,直接透传)。

### chatModel.call 内部(以 Anthropic 为例)

`AnthropicChatModel.call()` → `internalCall()`(`AnthropicChatModel.java:252、531`):

```java
public ChatResponse call(Prompt prompt) {
    Prompt requestPrompt = buildRequestPrompt(prompt);
    return this.internalCall(requestPrompt, null);   // 254
}

public ChatResponse internalCall(Prompt prompt, ChatResponse previousChatResponse) {
    MessageCreateParams request = createRequest(prompt, false);         // 533
    // 单次 HTTP 请求 + 解析,拿到含 tool_use 的响应也直接返回,不在这里执行工具
    Message message = anthropicClient.messages().withRawResponse().create(request).parse(); // 545-548
    List<Generation> generations = buildGenerations(message, ...);      // 561
    return new ChatResponse(generations, ...);                          // 570
}
```

**关键变化(Spring AI 2.0.0)**:`internalCall` 只做一次模型调用,**不再**在 ChatModel 内部跑工具执行循环。响应里若带 `tool_use`,原样返回给上层的 `ToolCallingAdvisor` 处理(见下一节)。

## 8. 工具调用循环:ToolCallingAdvisor(与 defaultTools 直接相关)

`CoordinatorAgent.java:38` 的 `.defaultTools(delegateTools)` 使 prompt 的 options 变为 `ToolCallingChatOptions`,从而激活工具循环。在 2.0.0,**工具循环不再藏在 ChatModel 里,而是一个显式的 advisor**:`ToolCallingAdvisor`(`ToolCallingAdvisor.java`)。

### 何时被加入链

`DefaultChatClient.buildAdvisorChain()` 第 1190 行先调 `autoRegisterToolCallingAdvisor()`(`DefaultChatClient.java:1217-1238`):
- 若用户没手动注册 `ToolAdvisor`,框架自动补一个 `ToolCallingAdvisor`。
- 自动注册**总是**发生(即使当前没配 tools),以便运行时动态注入的工具也能被处理。
- 是否让它自管历史,取决于是否存在 "下游 memory advisor":`a instanceof MemoryAdvisor && a.getOrder() > toolOrder`。

### order 决定它在链中的位置(本例的实际顺序)

| Advisor | order | 相对位置(request 方向,越小越外层) |
| --- | --- | --- |
| `MessageChatMemoryAdvisor` | `HIGHEST_PRECEDENCE + 200` | 外层 |
| `ToolCallingAdvisor` | `HIGHEST_PRECEDENCE + 300` | 中层 |
| `ChatModelCallAdvisor` | `LOWEST_PRECEDENCE` | 最内层(链尾) |

所以本例链的真实顺序是:

```
MessageChatMemoryAdvisor → ToolCallingAdvisor → ChatModelCallAdvisor
   (memory,+200)             (工具循环,+300)        (打 LLM,链尾)
```

注意 memory(+200)**不大于** tool(+300),因此第 1232-1233 行的 `hasDownstreamMemoryAdvisor = false`,工具循环以 `conversationHistoryEnabled = true` 构建——即**工具迭代过程中的中间消息由工具循环自己管理,不会每轮都进 `ChatMemory`**。memory advisor 在工具循环**之外**,只在整个 turn 的首尾各写一次(用户输入 / 最终助手回复)。

### 工具循环本体

`ToolCallingAdvisor.adviseCall()`(`ToolCallingAdvisor.java:120-188`)的完整结构:入口先判类型,循环前后各有一次性钩子,循环体内每轮打一次 LLM 并按需执行工具:

```java
public ChatClientResponse adviseCall(ChatClientRequest req, CallAdvisorChain chain) {
    // 0. 若 options 不是 ToolCallingChatOptions(模型不支持工具),直接透传,不进循环
    if (!(req.prompt().getOptions() instanceof ToolCallingChatOptions opts))
        return chain.nextCall(req);                                    // 126-129

    req = doInitializeLoop(req, chain);                                // 131  循环前钩子(仅一次)
    var instructions = req.prompt().getInstructions();
    ChatClientResponse response = null;
    boolean isToolCall = false;

    do {
        var processed = ChatClientRequest.builder()                    // 用当前 instructions 组装本轮请求
            .prompt(new Prompt(instructions, opts))
            .context(req.context()).build();                          // 142-145
        processed = doBeforeCall(processed, chain);                    // 148  每轮前钩子

        // copy(this):复制一条“不含自己”的链,穿到链尾 ChatModelCallAdvisor 打一次 LLM
        response = chain.copy(this).nextCall(processed);               // 150
        response = doAfterCall(response, chain);                       // 152  每轮后钩子

        ChatResponse chatResponse = response.chatResponse();
        isToolCall = toolExecutionEligibilityChecker.isToolCallResponse(chatResponse); // 有 tool_use?

        if (isToolCall) {
            // 在应用侧执行工具(本例即 DelegateTools 里的方法)
            ToolExecutionResult result = toolCallingManager
                .executeToolCalls(processed.prompt(), chatResponse);   // 161-162
            if (result.returnDirect()) { /* 包装工具结果 */ break; }   // 164-178  不回灌 LLM,直接返回
            // 把工具结果并入对话历史,作为下一轮 instructions 再次调用 LLM
            instructions = doGetNextInstructionsForToolCall(processed, response, result); // 180
        }
    } while (isToolCall);   // 直到某轮响应不再包含 tool_use

    return doFinalizeLoop(response, chain);                            // 187  循环后钩子(仅一次)
}
```

要点:
- **循环在 advisor 层**:每一轮 `chain.copy(this).nextCall(...)` 都会重新穿到链尾 `ChatModelCallAdvisor` 打一次 LLM。`copy(this)` 复制的是**排在自己之后**的 advisor(`DefaultAroundAdvisorChain.copyAdvisorsAfter`,第 178-195 行),避免把自己再算进去导致死循环。
- **工具在哪执行**:`toolCallingManager.executeToolCalls(...)`(第 161 行)。对本项目,这会触发 `DelegateTools` 上被 `@Tool` 标注的方法,即协调者把子任务委派给 Coder / Research / Reviewer 的实际落点。
- **退出条件**:模型某轮不再返回 `tool_use`(`isToolCall == false`),或某工具标注了 `returnDirect`。
- **与 memory 的配合**:因为 `ToolCallingAdvisor` 在 memory 内层,整个多轮工具循环对 `MessageChatMemoryAdvisor` 是"一次 nextCall"——所以 memory 只记录最终的助手回复,中间的 tool_use / tool_result 不落 `ChatMemory`(本例配置下)。

### 每轮 tool_use 结果如何被"保存"(conversationHistory 累积)

模型每次返回 `tool_use`,`executeToolCalls` 会把"本轮的 tool_use + 工具结果"追加到对话历史。见 `DefaultToolCallingManager.buildConversationHistoryAfterToolExecution`(`DefaultToolCallingManager.java:251-256`):

```java
List<Message> messages = new ArrayList<>(previousMessages); // 上一轮的全部历史
messages.add(assistantMessage);      // 本轮 LLM 的 tool_use(要调哪个工具、参数)
messages.add(toolResponseMessage);   // 本轮工具执行结果(tool_result,即 @Tool 方法返回值)
return messages;
```

因此在**一个 turn 内**,每轮追加两条消息、层层累积,再经 `doGetNextInstructionsForToolCall`(`ToolCallingAdvisor.java:190-202`)作为下一轮 `instructions` 喂回 LLM——模型据此知道"之前调了什么、得到了什么"。

关键在于这些中间消息的**生命周期与落库范围**:

| 问题 | 结论 |
| --- | --- |
| 每次 tool_use 结果会被保存吗? | **会**,在当前 turn 的 `conversationHistory` 里完整累积,供后续轮次与 LLM 使用 |
| 保存进 `ChatMemory`(跨 turn 持久化)了吗? | **没有**。本例 order 配置下,memory 只记录用户问题与最终回复 |
| 循环结束后还在吗? | **不在**。它是循环内的局部变量(`instructions`),`execute()` 返回即被丢弃 |

> `doGetNextInstructionsForToolCall` 还受 `conversationHistoryEnabled` 影响(`ToolCallingAdvisor.java:193-201`):为 `true`(本例)时返回完整累积历史;为 `false` 时只保留 `SystemMessage` + 最后一条,靠内层 memory advisor 补历史。

### 扩展钩子:想在工具执行前后插入逻辑,重写这些 protected 方法

`ToolCallingAdvisor` 专门留了 4 个 `protected` 钩子(`ToolCallingAdvisor.java:204-220`,默认实现均为原样返回),子类可重写以介入循环:

| 钩子 | 触发时机 | 典型用途 |
| --- | --- | --- |
| `doInitializeLoop` | 循环开始前(仅一次) | 初始化上下文、埋点开始 |
| `doBeforeCall` | 每轮 LLM 调用前(可能多次) | 改写本轮请求 |
| `doAfterCall` | 每轮 LLM 调用后、工具执行前(可能多次) | 观察/记录每轮响应 |
| `doFinalizeLoop` | 整个循环结束后(仅一次) | 最终结果后处理、审计、通知 |

注册自定义子类的方式:在 `CoordinatorAgent` 里用 `defaultAdvisors(...)` 注册你的 `ToolAdvisor` 子类。`autoRegisterToolCallingAdvisor()` 检测到链里已有 `ToolAdvisor`(`DefaultChatClient.java:1225-1226`)就会**跳过自动注册**,你的实现即生效(注意保持 order 仍在 memory 内层,默认 `+300`)。若只想拦截"每次工具执行"本身,另一条路是包装 `ToolCallingManager`(接口仅 `resolveToolDefinitions` / `executeToolCalls` 两个方法),在 `executeToolCalls` 返回后读/改 `result.conversationHistory()`。

## 9. 流式(stream)路径的差异

流式走 `BaseAdvisor.adviseStream()`(`BaseAdvisor.java:56-74`):

```java
return chatClientResponseFlux.map(response -> {
    if (AdvisorUtils.onFinishReason().test(response)) {   // 仅在收到结束原因的那一帧
        response = after(response, streamAdvisorChain);   // 才触发 after
    }
    return response;
});
```

即流式下 `after`(assistant 消息落库)只在**流结束**那一帧执行,而非每个分片。这与 `CoordinatorAgent.stream()` 中多余的空 `.messages()` 调用无关。
