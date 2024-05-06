package com.carservice.model;

import jakarta.persistence.Id;

import java.time.LocalTime;

public class Appointment {

    @Id
    private String id;
    private LocalTime startTime;
    private LocalTime endTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
