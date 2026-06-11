package cn.tx.aiagent.project.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSelectResponse {

    private Long id;

    private String code;

    private String name;
}
