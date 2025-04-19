package com.varro.statusmonitor.controller;

import com.varro.statusmonitor.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RestController
public class MonitorController {
    @Value("${spring.application.timezone}")
    private String timezoneName;

    @Value("${spring.application.statuses.max}")
    private int MAX_STATUS_LENGTH;

    private final AtomicReference<List<Status>> statuses = new AtomicReference<>(new ArrayList<>());

    @PostMapping("/monitor")
    public ResponseEntity<String> addStatus(@RequestBody Status status) {
        if (statuses.get() != null ) {
            if (statuses.get().size() >= MAX_STATUS_LENGTH) {
                statuses.get().remove(statuses.get().size() - 1);
            }
            log.info("Timezone: {}", timezoneName);
            status.setTimestamp(ZonedDateTime.now().withZoneSameInstant(ZoneId.of(timezoneName)));
            statuses.get().add(status);
            return new ResponseEntity<>("OK", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Statuses error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/monitor")
    public ResponseEntity<List<Status>> getStatuses() {
        return new ResponseEntity<>(statuses.get(), HttpStatus.OK);
    }
}
