package cn.tx.aiagent.warehouse.domain;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository {
    List<Warehouse> findAll();
    Optional<Warehouse> findById(Long id);
    Optional<Warehouse> findByCode(String code);
    Warehouse save(Warehouse warehouse);
    void update(Warehouse warehouse);
    void softDelete(Long id);
}
