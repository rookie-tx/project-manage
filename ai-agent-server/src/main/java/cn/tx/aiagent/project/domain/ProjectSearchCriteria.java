package cn.tx.aiagent.project.domain;

/** 项目列表查询条件，page/size 自动修正为合法范围 */
public record ProjectSearchCriteria(
        String keyword,
        Integer typeCode,
        Integer statusCode,
        int page,
        int size
) {
    public ProjectSearchCriteria {
        if (page < 1) page = 1;
        if (size < 1) size = 20;
    }
}
