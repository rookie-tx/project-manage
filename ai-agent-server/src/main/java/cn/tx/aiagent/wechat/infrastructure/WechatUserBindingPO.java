package cn.tx.aiagent.wechat.infrastructure;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WechatUserBindingPO {
    private Long id;
    private Long userId;
    private String openid;
    private String unionid;
    private String nickname;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
