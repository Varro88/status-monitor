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

    @Value("${spring.application.statuses.repeats}")
    private int STATUS_REPEATS;

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
        String source = newStatus.getSource();

        newStatus.setTimestamp(ZonedDateTime.now().withZoneSameInstant(ZoneId.of(timezoneName)));
        statuses.add(newStatus);

        long sameStatusNum = statuses.stream().filter( it -> it.getSource().equals(source)).count();

        while (sameStatusNum > STATUS_REPEATS) {
            statuses = removeOldest(statuses, source);
            sameStatusNum = statuses.stream().filter( it -> it.getSource().equals(source)).count();
        }

        while(statuses.size() > MAX_STATUS_LENGTH) {
            statuses = removeOldest(statuses, null);
        }

        try {
            monitorData.get().setStatuses(statuses);
            Utils.saveToJsonFile(monitorData, fileName);
        } catch (IOException e) {
            String errorText = String.format("Failed to save data to file: %s", e.getMessage());
            log.error(errorText, e);
            return new SimpleResponse(errorText, HttpStatus.BAD_REQUEST);
        }
        return new SimpleResponse("OK", HttpStatus.OK);
    }

    public List<Status> removeOldest(List<Status> items, String newStatus) {
        Status oldest;
        if(newStatus != null) {
            oldest = items.stream()
                    .filter(item -> item.getSource().equals(newStatus))
                    .min(Comparator.comparing(Status::getTimestamp))
                    .orElse(null);
        } else {
            oldest = items.stream()
                    .min(Comparator.comparing(Status::getTimestamp))
                    .orElse(null);
        }

        if (oldest == null) {
            return new ArrayList<>(items);
        }

        boolean[] removed = {false};
        return items.stream()
                .filter(item -> {
                    if (!removed[0] && item == oldest) {
                        removed[0] = true;
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }


    public MonitorData getMonitorData() {
        try {
            MonitorData data = Utils.readFromJsonFile(fileName, MonitorData.class);
            return new MonitorData(data.getSince(), data.getStatuses().stream()
                    .sorted(Comparator
                            .comparing(Status::getTimestamp)
                            .reversed())
                    .collect(Collectors.toList()));
        } catch (IOException exception) {
            log.warn("Failed to read file: {}", exception.getMessage());
            return monitorData.get();
        }
    }
}
