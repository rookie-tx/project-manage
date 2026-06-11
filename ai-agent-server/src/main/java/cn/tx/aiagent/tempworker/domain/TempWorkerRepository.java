package cn.tx.aiagent.tempworker.domain;

import java.util.List;
import java.util.Optional;

public interface TempWorkerRepository {
    List<TempWorker> findByProjectId(Long projectId);
    Optional<TempWorker> findById(Long id);
    Optional<TempWorker> findByUserId(Long userId);
    TempWorker save(TempWorker worker);
    void update(TempWorker worker);
    void softDelete(Long id);
}
