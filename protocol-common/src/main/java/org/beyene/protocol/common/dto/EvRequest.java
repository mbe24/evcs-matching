package org.beyene.protocol.common.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class EvRequest {
    public String id;
    public double energy;
    public LocalDate date;
    public LocalTime time;
    public int window;
}
