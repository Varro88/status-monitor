package com.varro.statusmonitor.model;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class Status {
    private String source;
    private ZonedDateTime timestamp;
}
