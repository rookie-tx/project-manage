package cn.tx.aiagent.agent.domain;

/**
 * Agent 对话状态。
 *
 * <pre>
 * IDLE → CLARIFYING → AWAITING_CONFIRMATION → COMPLETED
 *            ↑                                    │
 *            └── (信息仍不足，继续追问) ←──────────┘
 * </pre>
 */
public enum SessionState {
    /** 空闲，等待用户发起 */
    IDLE,
    /** 正在澄清信息（信息不足，追问用户） */
    CLARIFYING,
    /** 已生成草稿，等待用户确认 */
    AWAITING_CONFIRMATION,
    /** 对话完成（已提交审批或已取消） */
    COMPLETED
}
