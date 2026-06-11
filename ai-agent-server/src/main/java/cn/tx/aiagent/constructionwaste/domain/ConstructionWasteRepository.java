package cn.tx.aiagent.constructionwaste.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConstructionWasteRepository {
    ConstructionWaste save(ConstructionWaste waste);
    Optional<ConstructionWaste> findById(Long id);
    List<ConstructionWaste> search(Long projectId, String wasteType,
                                    LocalDate startDate, LocalDate endDate,
                                    int offset, int limit);
    long count(Long projectId, String wasteType,
               LocalDate startDate, LocalDate endDate);
    void update(ConstructionWaste waste);
    void softDelete(Long id);
}
