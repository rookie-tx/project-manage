package cn.tx.aiagent.dict.api;

import lombok.Data;

@Data
public class DictResponse {
    private String code;
    private String label;
    private Integer sort;
}
