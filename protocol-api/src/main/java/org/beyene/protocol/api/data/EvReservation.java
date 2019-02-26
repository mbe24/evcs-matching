package org.beyene.protocol.api.data;

import java.util.List;
import java.util.Objects;

public class EvReservation {

    public String id;
    public String requestId;
    public String offerId;
    public double price;
    public String payment;
    public List<String> paymentOptions;
    public CsReservation.Status status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EvReservation)) return false;
        EvReservation that = (EvReservation) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(requestId, that.requestId) &&
                Objects.equals(offerId, that.offerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, requestId, offerId);
    }
}
