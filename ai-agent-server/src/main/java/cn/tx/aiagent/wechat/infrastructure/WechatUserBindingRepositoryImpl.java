package cn.tx.aiagent.wechat.infrastructure;

import cn.tx.aiagent.wechat.domain.WechatUserBinding;
import cn.tx.aiagent.wechat.domain.WechatUserBindingRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WechatUserBindingRepositoryImpl implements WechatUserBindingRepository {
    private final WechatUserBindingMapper mapper;
    private final WechatUserBindingPoMapper poMapper;

    public WechatUserBindingRepositoryImpl(WechatUserBindingMapper mapper,
                                            WechatUserBindingPoMapper poMapper) {
        this.mapper = mapper;
        this.poMapper = poMapper;
    }

    @Override
    public WechatUserBinding save(WechatUserBinding binding) {
        WechatUserBindingPO po = poMapper.toPO(binding);
        mapper.insert(po);
        return poMapper.toDomain(mapper.selectByUserId(po.getUserId()));
    }

    @Override
    public Optional<WechatUserBinding> findByOpenid(String openid) {
        WechatUserBindingPO po = mapper.selectByOpenid(openid);
        return po == null ? Optional.empty() : Optional.of(poMapper.toDomain(po));
    }

    @Override
    public Optional<WechatUserBinding> findByUserId(Long userId) {
        WechatUserBindingPO po = mapper.selectByUserId(userId);
        return po == null ? Optional.empty() : Optional.of(poMapper.toDomain(po));
    }

    @Override
    public void deleteByUserId(Long userId) {
        mapper.deleteByUserId(userId);
    }
}
