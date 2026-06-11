package cn.tx.aiagent.wechat.infrastructure;

import cn.tx.aiagent.wechat.domain.WechatUserBinding;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WechatUserBindingPoMapper {

    default WechatUserBinding toDomain(WechatUserBindingPO po) {
        if (po == null) return null;
        return WechatUserBinding.fromPersistence(
                po.getId(), po.getUserId(), po.getOpenid(),
                po.getUnionid(), po.getNickname(), po.getAvatarUrl(),
                po.getCreatedAt()
        );
    }

    WechatUserBindingPO toPO(WechatUserBinding binding);
}
