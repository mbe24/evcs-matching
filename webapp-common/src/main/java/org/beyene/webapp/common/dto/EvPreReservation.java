package org.beyene.webapp.common.dto;

import java.util.List;

public class EvPreReservation {

    public long id;
    public long requestId;
    public long offerId;
    public double price;
    public String payment;
    public List<String> paymentOptions;
    public EvReservation.Status status;
}
