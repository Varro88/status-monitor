package com.varro.statusmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class Status {
    private String source;
    private ZonedDateTime timestamp;
}
