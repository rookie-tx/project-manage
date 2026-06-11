package cn.tx.aiagent.user.domain;

import java.util.List;

/**
 * 用户分页查询结果值对象。
 */
public record UserListResult(
        List<User> records,
        long total,
        int page,
        int size
) {
}
