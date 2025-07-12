package com.varro.statusmonitor.controller;

import com.varro.statusmonitor.MonitorService;
import com.varro.statusmonitor.model.MonitorData;
import com.varro.statusmonitor.model.SimpleResponse;
import com.varro.statusmonitor.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;

@Slf4j
@RestController
public class MonitorController {

    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @PostMapping("/monitor")
    public ResponseEntity<String> addStatus(@RequestBody Status status) {
        SimpleResponse resp = monitorService.addStatus(status);
        return new ResponseEntity<>(resp.getMessage(), resp.getHttpStatus());
    }

    @GetMapping("/monitor")
    public ResponseEntity<MonitorData> getStatuses() {
        return new ResponseEntity<>(monitorService.getMonitorData(), HttpStatus.OK);
    }

    @GetMapping("/monitor/legacy")
    public ResponseEntity<String> addStatusLegacy(@RequestParam(value = "source") String source,
                                                  @RequestParam(value = "timestamp", required = false) String timestamp) {
        ZonedDateTime zonedDateTime = timestamp == null ? ZonedDateTime.now()
                : ZonedDateTime.parse(timestamp.replace(" ","+"));
        SimpleResponse resp = monitorService.addStatus(new Status(source, zonedDateTime));
        return new ResponseEntity<>(resp.getMessage(), resp.getHttpStatus());
    }
}
