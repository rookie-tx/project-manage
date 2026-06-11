package cn.tx.aiagent.wechat.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WechatUserBindingMapper {
    void insert(WechatUserBindingPO po);
    WechatUserBindingPO selectByOpenid(@Param("openid") String openid);
    WechatUserBindingPO selectByUserId(@Param("userId") Long userId);
    void deleteByUserId(@Param("userId") Long userId);
}
