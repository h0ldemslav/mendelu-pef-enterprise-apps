package cz.mendelu.pef.airline_reservation_system.utils.enums;

import java.util.Optional;

public enum TicketClass {
    Business, Premium, Economy;

    public static Optional<TicketClass> getTicketClassByString(String string) {
        return switch (string) {
            case "Business" -> Optional.of(Business);
            case "Premium" -> Optional.of(Premium);
            case "Economy" -> Optional.of(Economy);
            default -> Optional.empty();
        };
    }
}
