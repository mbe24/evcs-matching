package org.beyene.protocol.common.dto;

import java.util.Objects;

public class CsReservation {
    public String id;
    public String requestId;
    public String offerId;
    public double price;
    public String payment;
    public Status status;

    public enum Status {
        OPEN, ACCEPTED, REJECTED, PAID;
    }

    public enum Operation {
        ACCEPT, REJECT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CsReservation)) return false;
        CsReservation that = (CsReservation) o;
        return Double.compare(that.price, price) == 0 &&
                Objects.equals(id, that.id) &&
                Objects.equals(requestId, that.requestId) &&
                Objects.equals(offerId, that.offerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, requestId, offerId, price);
    }
}
