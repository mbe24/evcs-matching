package org.beyene.webapp.common.dto;

public class EvReservation {
    public long id;
    public long requestId;
    public long offerId;
    public double price;
    public String payment;
    public Status status;

    public enum Status {
        OPEN, ACCEPTED, REJECTED, PAID;
    }

    public enum Operation {
        ACCEPT, REJECT;
    }
}
