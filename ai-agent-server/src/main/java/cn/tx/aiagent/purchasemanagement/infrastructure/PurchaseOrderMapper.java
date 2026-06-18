package cn.tx.aiagent.purchasemanagement.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PurchaseOrderMapper {

    void insert(PurchaseOrderPO po);

    PurchaseOrderPO selectById(@Param("id") Long id);

    List<PurchaseOrderPO> selectByPage(@Param("projectId") Long projectId,
                                        @Param("status") Integer status,
                                        @Param("tab") String tab,
                                        @Param("offset") int offset,
                                        @Param("size") int size);

    long countByFilter(@Param("projectId") Long projectId,
                       @Param("status") Integer status,
                       @Param("tab") String tab);

    PurchaseOrderPO selectByPRId(@Param("prId") Long prId);

    List<PurchaseOrderPO> selectByStatus(@Param("status") Integer status);

    void update(PurchaseOrderPO po);

    void softDelete(@Param("id") Long id);

    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    void updatePurchaser(@Param("id") Long id, @Param("purchaserId") Long purchaserId);

    void updateInboundOperator(@Param("id") Long id, @Param("inboundOperatorId") Long inboundOperatorId);

    boolean existsByPurchaseRequestId(@Param("purchaseRequestId") Long purchaseRequestId);
}
