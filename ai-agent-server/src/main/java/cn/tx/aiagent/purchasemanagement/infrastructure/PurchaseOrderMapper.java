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
                                        @Param("offset") int offset,
                                        @Param("size") int size);

    long countByFilter(@Param("projectId") Long projectId,
                       @Param("status") Integer status);

    void update(PurchaseOrderPO po);

    void softDelete(@Param("id") Long id);

    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    boolean existsByPurchaseRequestId(@Param("purchaseRequestId") Long purchaseRequestId);

    void insertItem(@Param("orderId") Long orderId, @Param("requestId") Long requestId);

    List<Long> selectRequestIdsByOrderId(@Param("orderId") Long orderId);

    void deleteItemsByOrderId(@Param("orderId") Long orderId);
}
