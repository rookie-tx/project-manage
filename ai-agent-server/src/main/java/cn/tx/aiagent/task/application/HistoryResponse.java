package cn.tx.aiagent.task.application;

import java.time.LocalDateTime;

public record HistoryResponse(String nodeName, String assignee, String assigneeName,
                               String result, String comment, LocalDateTime time) {}
