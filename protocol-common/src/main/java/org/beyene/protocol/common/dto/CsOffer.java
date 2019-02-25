package org.beyene.protocol.common.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class CsOffer {
    public String id;
    public double price;
    public double energy;
    public LocalDate date;
    public LocalTime time;
    public int window;
    public boolean reserved;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CsOffer)) return false;
        CsOffer csOffer = (CsOffer) o;
        return Objects.equals(id, csOffer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
