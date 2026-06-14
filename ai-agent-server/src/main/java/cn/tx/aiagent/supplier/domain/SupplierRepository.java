package cn.tx.aiagent.supplier.domain;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository {
    List<Supplier> findAll(String keyword, Integer statusCode, int offset, int limit);
    long count(String keyword, Integer statusCode);
    Optional<Supplier> findById(Long id);
    Optional<Supplier> findByUserId(Long userId);
    void save(Supplier supplier);
    void update(Supplier supplier);
    void softDelete(Long id);
}
