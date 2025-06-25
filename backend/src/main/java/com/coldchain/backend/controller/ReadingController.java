package com.coldchain.backend.controller;

import com.coldchain.backend.model.Reading;
import com.coldchain.backend.repository.ReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/readings")
public class ReadingController {

    private final ReadingRepository readingRepository;

    @Autowired
    public ReadingController(ReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    // GET /api/readings/device/123
    @GetMapping("/device/{deviceId}")
    public List<Reading> getReadingsByDeviceId(@PathVariable String deviceId) {
        return readingRepository.findByDeviceIdOrderByTimestampDesc(deviceId);
    }
}
