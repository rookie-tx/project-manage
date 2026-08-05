package cn.tx.aiagent.warehouse.domain;

import java.util.List;
import java.util.Optional;

public interface MaterialRepository {
    List<Material> findAll();
    Optional<Material> findById(Long id);
    Optional<Material> findByCode(String code);
    Material save(Material material);
    void update(Material material);
    void softDelete(Long id);

    /** 按名称或规格模糊搜索，上限 10 条，供 Agent 工具调用 */
    List<Material> findByNameOrSpecLike(String keyword);
}
