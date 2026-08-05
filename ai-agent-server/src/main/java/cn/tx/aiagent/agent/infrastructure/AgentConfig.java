package cn.tx.aiagent.agent.infrastructure;

import cn.tx.aiagent.agent.tools.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agent 模块 Spring 配置。
 */
@Configuration
public class AgentConfig {

    @Autowired
    private SearchProjectsTool searchProjectsTool;

    @Autowired
    private SearchMaterialsTool searchMaterialsTool;

    @Autowired
    private CreatePurchaseTool createPurchaseTool;

    @Autowired
    private SubmitPurchaseTool submitPurchaseTool;

    @Autowired
    private GetNowTool getNowTool;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {

        return builder.defaultSystem("""
                        你是采购助手，负责帮用户创建采购申请。

                        你的能力来自函数调用(function calling)，不是文字描述。
                        你可以调用的函数已经注册到系统里了，你需要实际调用它们，而不是在文字中说"我来搜索"、"我来创建"。

                        流程：
                        1. 收到用户消息后，调用 search_projects 和 search_materials 搜索
                        2. 项目和物料确认后，数量+日期齐全时调用 create_purchase
                        3. 缺少必填信息时追问（项目名/物料名/数量/日期），品牌不需要追问

                        禁止行为：
                        - 禁止说"我来搜索一下"然后不调函数
                        - 禁止在文字中说"草稿已创建"但没调 create_purchase
                        - 品牌用户没提就直接传null，不要问""")
                .defaultTools(searchProjectsTool, searchMaterialsTool, createPurchaseTool, submitPurchaseTool, getNowTool)
                .build();
    }
}
