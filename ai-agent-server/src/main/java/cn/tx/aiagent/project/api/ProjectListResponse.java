package cn.tx.aiagent.project.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** 项目分页列表响应 */
@Data
@AllArgsConstructor
public class ProjectListResponse {

    private List<ProjectResponse> items;
    private long total;
    private int page;
    private int size;
}
