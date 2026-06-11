package cn.tx.aiagent.file.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UploadFileMapper {
    void insert(UploadFilePO po);
    UploadFilePO selectById(@Param("id") Long id);
    List<UploadFilePO> selectByBusiness(@Param("businessType") String businessType,
                                         @Param("businessId") Long businessId);
    void updateBusinessId(@Param("id") Long id,
                          @Param("businessType") String businessType,
                          @Param("businessId") Long businessId);
    void deleteById(@Param("id") Long id);
    void deleteByBusiness(@Param("businessType") String businessType,
                          @Param("businessId") Long businessId);
}
