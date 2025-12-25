package com.secondwind.controller;

import com.secondwind.dto.RunningRecordDTO;
import com.secondwind.entity.RunningRecord;
import com.secondwind.entity.UserAuth;
import com.secondwind.repository.RunningRecordRepository;
import com.secondwind.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/running")
public class RunningRecordController {

    private final RunningRecordRepository runningRecordRepository;
    private final UserRepository userRepository;

    public RunningRecordController(RunningRecordRepository runningRecordRepository, UserRepository userRepository) {
        this.runningRecordRepository = runningRecordRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncRecord(@RequestBody RunningRecordDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth user = userRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        Optional<RunningRecord> existingRecord = runningRecordRepository.findBySessionId(dto.getSessionId());
        RunningRecord record;

        if (existingRecord.isPresent()) {
            record = existingRecord.get();
        } else {
            record = new RunningRecord();
            record.setSessionId(dto.getSessionId());
            record.setUser(user);
        }

        record.setDistance(dto.getDistance());
        record.setDuration(dto.getDuration());
        record.setAverageSpeed(dto.getSpeed());
        record.setAveragePace(dto.getPace());
        record.setRoute(dto.getRoute());
        record.setWateringSegments(dto.getWateringSegments());
        record.setSplits(dto.getSplits());
        record.setIsComplete(dto.getIsComplete());

        runningRecordRepository.save(record);

        return ResponseEntity.ok().build();
    }
}
