package org.beyene.protocol.api.data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class EvRequest {
    public String id;
    public double energy;
    public LocalDate date;
    public LocalTime time;
    public int window;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EvRequest)) return false;
        EvRequest evRequest = (EvRequest) o;
        return Double.compare(evRequest.energy, energy) == 0 &&
                window == evRequest.window &&
                Objects.equals(id, evRequest.id) &&
                Objects.equals(date, evRequest.date) &&
                Objects.equals(time, evRequest.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, energy, date, time, window);
    }

    @Override
    public String toString() {
        return "EvRequest{" +
                "id='" + id + '\'' +
                ", energy=" + energy +
                ", date=" + date +
                ", time=" + time +
                ", window=" + window +
                '}';
    }
}
