package cn.tx.aiagent.wechat.api;

import lombok.Data;

import java.util.List;

@Data
public class WechatLoginResponse {
    private String token;
    private boolean needBind;
    private UserInfo user;

    public static WechatLoginResponse success(String token, long userId, String username, String name, List<String> permissions) {
        WechatLoginResponse resp = new WechatLoginResponse();
        resp.token = token;
        resp.needBind = false;
        resp.user = new UserInfo(userId, username, name, permissions);
        return resp;
    }

    public static WechatLoginResponse needBind() {
        WechatLoginResponse resp = new WechatLoginResponse();
        resp.needBind = true;
        return resp;
    }

    @Data
    public static class UserInfo {
        private final long id;
        private final String username;
        private final String name;
        private final List<String> permissions;
    }
}
