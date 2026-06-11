package cn.tx.aiagent.user.domain;

/**
 * 用户查询条件值对象。
 */
public record UserSearchCriteria(
        String keyword,
        Integer statusCode,
        int page,
        int size
) {
    public UserSearchCriteria {
        if (page < 1) page = 1;
        if (size < 1) size = 10;
    }

    public int offset() {
        return (page - 1) * size;
    }
}
