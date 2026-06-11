package cn.tx.aiagent.project.api;

import lombok.Data;

/**
 * 项目列表查询请求，Spring MVC 自动绑定 query string
 */
@Data
public class ProjectListRequest {

    private String keyword;
    private Integer type;
    private Integer status;
    private int page = 1;
    private int size = 20;
}
