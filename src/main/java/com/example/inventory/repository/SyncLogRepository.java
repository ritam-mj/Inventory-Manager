package com.example.inventory.repository;

import com.example.inventory.domain.sync.SyncLog;
import com.example.inventory.domain.sync.SyncStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncLogRepository extends JpaRepository<SyncLog, Long> {
    List<SyncLog> findTop100ByStatusOrderByCreatedAtAsc(SyncStatus status);
}
