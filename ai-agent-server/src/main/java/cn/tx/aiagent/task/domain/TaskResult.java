package cn.tx.aiagent.task.domain;

public sealed interface TaskResult {

    record Approved() implements TaskResult {}

    record Rejected() implements TaskResult {}
}
