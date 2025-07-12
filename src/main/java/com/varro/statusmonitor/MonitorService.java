package com.varro.statusmonitor;

import com.varro.statusmonitor.model.MonitorData;
import com.varro.statusmonitor.model.SimpleResponse;
import com.varro.statusmonitor.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MonitorService {
    @Value("${spring.application.timezone}")
    private String timezoneName;

    @Value("${spring.application.statuses.max}")
    private int MAX_STATUS_LENGTH;

    @Value("${spring.application.file.name}")
    private String fileName;

    private final AtomicReference<MonitorData> monitorData = new AtomicReference<>(new MonitorData());

    public SimpleResponse addStatus(Status status) {
        if (monitorData.get() != null ) {
            return processNewStatus(status);
        } else {
            return new SimpleResponse("Statuses error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private SimpleResponse processNewStatus(Status newStatus) {
        List<Status> statuses = monitorData.get().getStatuses();

        if(statuses.size() >= MAX_STATUS_LENGTH) {
            long uniqueStatuses = statuses.stream().map(Status::getSource).distinct().count();
            if(uniqueStatuses == statuses.size() || uniqueStatuses == 1) {
                statuses.remove(statuses.get(0));
            } else {
                Map<String, Integer> occurrences = new HashMap<>();
                for (Status s : statuses) {
                    if(occurrences.containsKey(s.getSource())) {
                        occurrences.put(s.getSource(), occurrences.get(s.getSource()) + 1);
                    } else  {
                        occurrences.put(s.getSource(), 1);
                    }
                }

                Set<String> twoOrMore = occurrences.entrySet().stream()
                        .filter(x -> x.getValue() > 1)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet());

                if(!twoOrMore.isEmpty()) {
                    Status statusToRemove = null;

                    for (Status status : statuses) {
                        if (!twoOrMore.contains(status.getSource())) {
                            continue;
                        }

                        if (statusToRemove == null) {
                            statusToRemove = status;
                        }

                        if (statusToRemove.getTimestamp().isAfter(status.getTimestamp())) {
                            statusToRemove = status;
                        }
                    }
                    statuses.remove(statusToRemove);

                } else {
                    statuses.remove(statuses.get(0));
                }
            }
        }
        newStatus.setTimestamp(ZonedDateTime.now().withZoneSameInstant(ZoneId.of(timezoneName)));
        statuses.add(newStatus);
        try {
            Utils.saveToJsonFile(monitorData, fileName);
        } catch (IOException e) {
            log.error("Failed to save data to file: {}", e.getMessage());
        }
        return new SimpleResponse("OK", HttpStatus.OK);
    }

    public MonitorData getMonitorData() {
        MonitorData data;

        try {
            data = Utils.readFromJsonFile(fileName, MonitorData.class);
        } catch (IOException exception) {
            log.warn("Failed to read file: {}", exception.getMessage());
            data = monitorData.get();
        }

        return new MonitorData(data.getSince(), data.getStatuses().stream()
                .sorted(Comparator.comparing(Status::getTimestamp)
                        .reversed())
                .collect(Collectors.toList()));
    }
}
