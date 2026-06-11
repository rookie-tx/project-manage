package cn.tx.aiagent.wechat.domain;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class WechatUserBinding {
    private Long id;
    private Long userId;
    private String openid;
    private String unionid;
    private String nickname;
    private String avatarUrl;
    private LocalDateTime createdAt;

    private WechatUserBinding() {}

    public static WechatUserBinding create(Long userId, String openid, String unionid) {
        WechatUserBinding b = new WechatUserBinding();
        b.userId = userId;
        b.openid = openid;
        b.unionid = unionid;
        return b;
    }

    public static WechatUserBinding fromPersistence(Long id, Long userId, String openid,
                                                     String unionid, String nickname,
                                                     String avatarUrl, LocalDateTime createdAt) {
        WechatUserBinding b = new WechatUserBinding();
        b.id = id;
        b.userId = userId;
        b.openid = openid;
        b.unionid = unionid;
        b.nickname = nickname;
        b.avatarUrl = avatarUrl;
        b.createdAt = createdAt;
        return b;
    }
}
