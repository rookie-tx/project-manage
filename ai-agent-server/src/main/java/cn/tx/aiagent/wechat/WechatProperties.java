package cn.tx.aiagent.wechat;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "app.wechat.miniapp")
public class WechatProperties {
    private String appid = "placeholder";
    private String secret = "placeholder";
}
