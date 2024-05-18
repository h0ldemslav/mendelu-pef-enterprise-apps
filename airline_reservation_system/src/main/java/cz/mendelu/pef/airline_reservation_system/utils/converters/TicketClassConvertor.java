package cz.mendelu.pef.airline_reservation_system.utils.converters;

import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.InvalidTicketClassException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TicketClassConvertor implements AttributeConverter<TicketClass, String> {
    @Override
    public String convertToDatabaseColumn(TicketClass ticketClass) {
        if (ticketClass == null) {
            return null;
        }

        return ticketClass.name();
    }

    @Override
    public TicketClass convertToEntityAttribute(String s) {
        TicketClass ticketClass;

        try {
            ticketClass = TicketClass.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw new InvalidTicketClassException();
        }

        return ticketClass;
    }
}
