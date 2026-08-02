package com.mba.fc.ingressos.events.orders;

public record ReserveSpotRequest(
        String eventId, String sectionId, String spotId, String customerId, String cardToken) {
}
