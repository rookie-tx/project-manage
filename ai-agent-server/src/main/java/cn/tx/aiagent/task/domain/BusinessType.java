package cn.tx.aiagent.task.domain;

import lombok.Getter;

@Getter
public enum BusinessType {

    PURCHASE_REQUEST("purchase-request-approval", "采购申请");

    private final String processDefinitionKey;

    private final String name;

    BusinessType(String processDefinitionKey, String name) {
        this.processDefinitionKey = processDefinitionKey;
        this.name = name;
    }

}
