package org.beyene.protocol.common.dto;

import java.util.List;

public class EvReservation {

    public String id;
    public String requestId;
    public String offerId;
    public double price;
    public String payment;
    public List<String> paymentOptions;
    public CsReservation.Status status;
}
