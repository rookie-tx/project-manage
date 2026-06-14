package cn.tx.aiagent.supplier.api;

import lombok.Data;

import java.util.List;

/**
 * 供应商注册响应 DTO（包含 JWT token、用户信息和供应商信息）。
 */
@Data
public class SupplierRegisterResponse {

    private String token;
    private UserInfo user;
    private SupplierResponse supplier;

    public static SupplierRegisterResponse success(String token, long userId, String username, String name,
                                                    List<String> permissions, SupplierResponse supplier) {
        SupplierRegisterResponse resp = new SupplierRegisterResponse();
        resp.token = token;
        resp.user = new UserInfo(userId, username, name, permissions);
        resp.supplier = supplier;
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
