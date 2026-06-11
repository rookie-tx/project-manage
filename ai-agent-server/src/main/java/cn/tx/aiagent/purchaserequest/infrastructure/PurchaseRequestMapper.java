package cn.tx.aiagent.purchaserequest.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PurchaseRequestMapper {

    void insert(PurchaseRequestPO po);

    PurchaseRequestPO selectById(@Param("id") Long id);

    List<PurchaseRequestPO> selectByPage(@Param("projectId") Long projectId,
                                          @Param("createdBy") Long createdBy,
                                          @Param("offset") int offset,
                                          @Param("size") int size);

    long countByProject(@Param("projectId") Long projectId,
                        @Param("createdBy") Long createdBy);

    void update(PurchaseRequestPO po);

    void softDelete(@Param("id") Long id);

    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    String findMaxCodeByPrefix(@Param("prefix") String prefix);

    List<PurchaseRequestPO> selectAvailable();

    boolean existsInPurchaseOrder(@Param("id") Long id);
}
