package cn.tx.aiagent.purchaserequest.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestListResponse {

    private List<PurchaseRequestResponse> items;
    private long total;
    private int page;
    private int size;
}
