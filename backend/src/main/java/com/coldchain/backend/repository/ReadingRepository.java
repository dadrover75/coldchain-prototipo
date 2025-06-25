package com.coldchain.backend.repository;

import com.coldchain.backend.model.Reading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingRepository extends JpaRepository<Reading, Long> {
    List<Reading> findByDeviceIdOrderByTimestampDesc(String deviceId);
}
