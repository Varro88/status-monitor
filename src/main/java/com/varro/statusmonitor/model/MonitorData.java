package com.varro.statusmonitor.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class MonitorData {
    private ZonedDateTime since;
    private List<Status> statuses;

    public MonitorData() {
        this.since = ZonedDateTime.now();
        statuses = new ArrayList<>();
    }

    public MonitorData(ZonedDateTime since, List<Status> statuses) {
        this.since = since;
        this.statuses = statuses;
    }
}
