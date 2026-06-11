package cn.tx.aiagent.wechat.domain;

import java.util.Optional;

public interface WechatUserBindingRepository {
    WechatUserBinding save(WechatUserBinding binding);
    Optional<WechatUserBinding> findByOpenid(String openid);
    Optional<WechatUserBinding> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
