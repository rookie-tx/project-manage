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
}
