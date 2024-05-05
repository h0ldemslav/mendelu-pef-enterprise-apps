package cz.mendelu.pef.airline_reservation_system.domain.flight;

import cz.mendelu.pef.airline_reservation_system.domain.aircraft.Aircraft;
import cz.mendelu.pef.airline_reservation_system.domain.fare_tariff.FareTariff;
import cz.mendelu.pef.airline_reservation_system.domain.ticket.Ticket;
import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.lang.Integer.parseInt;

@Service
public class FlightService {

    private FlightRepository flightRepository;

    FlightService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    public List<Flight> getAllFlights(Pageable pageRequest) {
        var flights = flightRepository
                .findAll(pageRequest)
                .getContent();

        return flights.stream().filter(fl -> {
            Aircraft aircraft = fl.getAircraft();

            if (aircraft == null) {
                return false;
            }

            var ticketTotalNumber = fl.getTickets().size();
            var aircraftTotalCapacityNumber = aircraft.getBusinessCapacity()
                    + aircraft.getPremiumCapacity() + aircraft.getEconomyCapacity();

            // Return only available flights
            return ticketTotalNumber < aircraftTotalCapacityNumber;
        }).toList();
    }

    /**
     *
     * @param id - id of existing fare tariff
     * @return list of all flights associating with the fare tariff. NOTE: it's not paginated.
     */
    public List<Flight> getAllFlightsByFareTariffId(Long id) {
        List<Flight> flights = new ArrayList<>();
        flightRepository.getFlightsByFareTariff_IdEquals(id).forEach(flights::add);

        return flights;
    }

    public Optional<Flight> getFlightById(Long id) {
        return flightRepository.findById(id);
    }

    public Flight createFlight(Flight flight) {
        return flightRepository.save(flight);
    }

    public Flight updateFlight(Long id, Flight flight) {
        flight.setId(id);
        return flightRepository.save(flight);
    }

    public void deleteFlightById(Long id) {
        flightRepository.deleteById(id);
    }

    public void setFareTariff(FareTariff fareTariff, List<Flight> flights) {
        flights.forEach(fl -> fl.setFareTariff(fareTariff));
        flightRepository.saveAll(flights);
    }

    public Optional<String> getSeatNumber(Flight flight, TicketClass ticketClass) {
        if (flight == null || flight.getAircraft() == null) {
            return Optional.empty();
        }

        final Aircraft aircraft = flight.getAircraft();
        final List<String> occupiedSeatNumbers = flight
                .getTickets()
                .stream()
                .filter(t -> Objects.equals(t.getTicketClass(), ticketClass.name()) && t.getSeatNumber() != null)
                .map(Ticket::getSeatNumber)
                .toList();
        final List<String> allowedSeatLetters = List.of("A", "B", "C", "D", "E", "F");
        int seatNumberRow = 1;
        int ticketClassCapacity = flight.getAircraft().getCapacityByTicketClass(ticketClass);

        switch (ticketClass) {
            case Business:
                break;
            case Premium:
                    seatNumberRow += aircraft.getTotalNumberOfSeatRows(TicketClass.Business, allowedSeatLetters.size());
                    break;
            case Economy:
                seatNumberRow += aircraft.getTotalNumberOfSeatRows(TicketClass.Premium, allowedSeatLetters.size())
                        + aircraft.getTotalNumberOfSeatRows(TicketClass.Business, allowedSeatLetters.size());
                break;
        }

        while (ticketClassCapacity > 0) {
            for (String letter : allowedSeatLetters) {
                var seatNumber = seatNumberRow + letter;

                if (!occupiedSeatNumbers.contains(seatNumber)) {
                    return Optional.of(seatNumber);
                }
            }

            seatNumberRow++;
            ticketClassCapacity--;
        }

        return Optional.empty();
    }

    public boolean isTicketClassSeatsAvailable(Flight flight, TicketClass ticketClass) {
        if (flight == null) {
            return false;
        }

        var aircraft = flight.getAircraft();
        if (aircraft == null) {
            return false;
        }

        var tickets = flight.getTickets()
                .stream()
                .filter(t -> t.getId() != null && Objects.equals(t.getTicketClass(), ticketClass.name()))
                .toList();

        return tickets.size() < aircraft.getCapacityByTicketClass(ticketClass);
    }

    public boolean isSeatNumberValid(Flight flight, TicketClass ticketClass, String seatNumber) {
        if (seatNumber == null || flight == null || flight.getAircraft() == null) {
            return false;
        }

        var seatNumberAndLetter = seatNumber.trim().split("(?<=\\d)(?=[A-Z])");

        if (seatNumberAndLetter.length != 2) {
            return false;
        }

        int number = 0;
        String letter = seatNumberAndLetter[1].toUpperCase();
        final List<String> allowedSeatLetters = List.of("A", "B", "C", "D", "E", "F");

        if (!allowedSeatLetters.contains(letter)) {
            return false;
        }

        try {
            number = parseInt(seatNumberAndLetter[0]);
        } catch (NumberFormatException e) {
            return false;
        }

        var aircraft = flight.getAircraft();
        var ticketClassSeatRowNumberStart = 0;
        var ticketClassSeatRowNumberEnd = 0;

        switch (ticketClass) {
            case Business:
                ticketClassSeatRowNumberStart = 1;
                ticketClassSeatRowNumberEnd = aircraft.getTotalNumberOfSeatRows(TicketClass.Business, allowedSeatLetters.size());
                break;
            case Premium:
                var premiumEnd = aircraft.getTotalNumberOfSeatRows(TicketClass.Premium, allowedSeatLetters.size());

                ticketClassSeatRowNumberStart = 1 + aircraft.getTotalNumberOfSeatRows(TicketClass.Business, allowedSeatLetters.size());
                ticketClassSeatRowNumberEnd = ticketClassSeatRowNumberStart + (premiumEnd - 1);
                break;
            case Economy:
                var economyEnd = aircraft.getTotalNumberOfSeatRows(TicketClass.Economy, allowedSeatLetters.size());

                ticketClassSeatRowNumberStart = 1 + aircraft.getTotalNumberOfSeatRows(TicketClass.Premium, allowedSeatLetters.size())
                        + aircraft.getTotalNumberOfSeatRows(TicketClass.Business, allowedSeatLetters.size());
                ticketClassSeatRowNumberEnd = ticketClassSeatRowNumberStart + (economyEnd - 1);
                break;
        }

        return number >= ticketClassSeatRowNumberStart && number <= ticketClassSeatRowNumberEnd;
    }

    /**
     * This method does not validate the seat number!
     */
    public boolean isSeatNumberOccupied(Flight flight, String seatNumber) {
        if (flight == null) {
            return true;
        }

        var ticketWithSeatNumber = flight.getTickets()
                .stream()
                .filter(t -> t.getId() != null && Objects.equals(t.getSeatNumber(), seatNumber.trim()))
                .findFirst();

        return ticketWithSeatNumber.isPresent();
    }
}
