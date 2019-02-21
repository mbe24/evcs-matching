package org.beyene.protocol.common.dto;

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
}
