package cn.tx.aiagent.wechat;

import lombok.Getter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class WechatApiClient {

    private static final String CODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final WechatProperties wechatProperties;
    private final HttpClient httpClient;

    public WechatApiClient(WechatProperties wechatProperties) {
        this.wechatProperties = wechatProperties;
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * 通过微信 code 换取 openid 和 session_key。
     * @return WechatSession 包含 openid, sessionKey, unionid(可选)
     * @throws WechatApiException 微信 API 返回错误或网络异常
     */
    public WechatSession code2Session(String code) throws WechatApiException {
        String url = String.format(CODE2SESSION_URL,
                wechatProperties.getAppid(),
                wechatProperties.getSecret(),
                code);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = OBJECT_MAPPER.readTree(response.body());

            if (json.has("errcode") && json.get("errcode").asInt() != 0) {
                int errcode = json.get("errcode").asInt();
                String errmsg = json.has("errmsg") ? json.get("errmsg").asString() : "unknown";
                throw new WechatApiException(errcode, errmsg);
            }

            String openid = json.get("openid").asString();
            String sessionKey = json.get("session_key").asString();
            String unionid = json.has("unionid") ? json.get("unionid").asString() : null;

            return new WechatSession(openid, sessionKey, unionid);
        } catch (WechatApiException e) {
            throw e;
        } catch (Exception e) {
            throw new WechatApiException(-1, "微信API调用失败: " + e.getMessage());
        }
    }

    public record WechatSession(String openid, String sessionKey, String unionid) {}

    @Getter
    public static class WechatApiException extends RuntimeException {
        private final int errcode;
        public WechatApiException(int errcode, String message) {
            super(message);
            this.errcode = errcode;
        }
    }
}
