package com.secondwind.repository;

import com.secondwind.entity.RunningRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RunningRecordRepository extends JpaRepository<RunningRecord, Long> {
    Optional<RunningRecord> findBySessionId(String sessionId);
}
