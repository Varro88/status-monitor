package com.varro.statusmonitor;

import com.varro.statusmonitor.model.SimpleResponse;
import com.varro.statusmonitor.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class MonitorService {
    @Value("${spring.application.timezone}")
    private String timezoneName;

    @Value("${spring.application.statuses.max}")
    private int MAX_STATUS_LENGTH;

    private final AtomicReference<List<Status>> statuses = new AtomicReference<>(new ArrayList<>());

    public SimpleResponse addStatus(Status status) {
        if (statuses.get() != null ) {
            return processNewStatus(status);
        } else {
            return new SimpleResponse("Statuses error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private SimpleResponse processNewStatus(Status status) {
        if(statuses.get().size() == MAX_STATUS_LENGTH) {
            long uniqueStatuses = statuses.get().stream().map(Status::getSource).distinct().count();
            if(uniqueStatuses == statuses.get().size() || uniqueStatuses == 1) {
                statuses.get().remove(0);
            } else {
                String mostOftenStatus = findMostPopularStatus(statuses.get());
                if (mostOftenStatus != null) {
                    for (int i = 0; i < statuses.get().size() ; i++) {
                        if (mostOftenStatus.equals(statuses.get().get(i).getSource())) {
                            statuses.get().remove(i);
                            break;
                        }
                    }
                }
                if(statuses.get().size() == MAX_STATUS_LENGTH) {
                    statuses.get().remove(0);
                }
            }
        }
        status.setTimestamp(ZonedDateTime.now().withZoneSameInstant(ZoneId.of(timezoneName)));
        statuses.get().add(status);
        return new SimpleResponse("OK", HttpStatus.OK);
    }

    private String findMostPopularStatus(List<Status> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (Status item : items) {
            frequencyMap.put(item.getSource(), frequencyMap.getOrDefault(item.getSource(), 0) + 1);
        }
        String mostPopular = null;
        int maxFrequency = 0;
        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > maxFrequency) {
                maxFrequency = entry.getValue();
                mostPopular = entry.getKey();
            }
        }
        return mostPopular;
    }

    public List<Status> getStatuses() {
        return statuses.get();
    }
}
