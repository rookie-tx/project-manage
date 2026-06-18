package cn.tx.aiagent.quotation.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报价 MyBatis Mapper 接口
 */
@Mapper
public interface QuotationMapper {

    void insert(QuotationPO po);

    QuotationPO selectById(@Param("id") Long id);

    QuotationPO selectByOrderAndSupplier(@Param("purchaseOrderId") Long purchaseOrderId,
                                          @Param("supplierId") Long supplierId);

    List<QuotationPO> selectByPage(@Param("purchaseOrderId") Long purchaseOrderId,
                                    @Param("offset") int offset,
                                    @Param("size") int size);

    long countByPurchaseOrder(@Param("purchaseOrderId") Long purchaseOrderId);

    List<QuotationPO> selectByCreatedBy(@Param("createdBy") Long createdBy,
                                         @Param("offset") int offset,
                                         @Param("size") int size);

    long countByCreatedBy(@Param("createdBy") Long createdBy);

    void update(QuotationPO po);

    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    void softDelete(@Param("id") Long id);
}
