package cn.tx.aiagent.purchasemanagement.application;

import cn.tx.aiagent.purchasemanagement.api.PurchaseOrderResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PurchaseOrderListResponse {
    private List<PurchaseOrderResponse> items;
    private long total;
    private int page;
    private int size;
}
