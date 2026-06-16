package cn.tx.aiagent.dict.infrastructure;

import lombok.Data;

@Data
public class DictPO {
    private Long id;
    private String type;
    private String code;
    private String label;
    private Integer sort;
    private Integer enabled;
}
