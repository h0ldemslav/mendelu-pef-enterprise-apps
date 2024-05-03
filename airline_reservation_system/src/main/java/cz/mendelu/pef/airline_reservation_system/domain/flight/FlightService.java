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

    public String getSeatNumber(Flight flight, TicketClass ticketClass) {
        if (flight.getAircraft() == null) {
            return null;
        }

        var tickets = flight.getTickets()
                .stream()
                .filter(t -> Objects.equals(t.getTicketClass(), ticketClass.name()) && t.getSeatNumber() != null)
                .sorted(Comparator.comparing(Ticket::getSeatNumber))
                .toList();

        if (tickets.isEmpty()) {
            return switch (ticketClass) {
                case Business -> "1A";
                case Premium -> String.valueOf(flight.getAircraft().getBusinessCapacity() + 1) + 'A';
                case Economy -> String.valueOf(flight.getAircraft().getPremiumCapacity() + 1) + 'A';
            };
        }

        var lastSeat = tickets.get(tickets.size() - 1)
                .getSeatNumber()
                .split("(?<=\\d)(?=[A-Z])");

        // Checking if seat letter is the last letter in the seat row in aircraft
        if (Objects.equals(lastSeat[1], "F")) {
            return (parseInt(lastSeat[0]) + 1) + "A";
        }

        return lastSeat[0] + (char) (lastSeat[1].charAt(0) + 1);
    }

    public boolean isTicketClassSeatsAvailable(Flight flight, TicketClass ticketClass) {
        var aircraft = flight.getAircraft();

        if (aircraft == null) {
            return false;
        }

        var tickets = flight.getTickets()
                .stream()
                .filter(t -> Objects.equals(t.getTicketClass(), ticketClass.name()))
                .toList();

        return tickets.size() < aircraft.getCapacityByTicketClass(ticketClass);
    }

    public boolean isSeatNumberValid(Flight flight, TicketClass ticketClass, String seatNumber) {
        if (seatNumber == null || flight.getAircraft() == null) {
            return false;
        }

        var seatNumberAndLetter = seatNumber.trim().split("(?<=\\d)(?=[A-Z])");

        if (seatNumberAndLetter.length != 2) {
            return false;
        }

        int number = 0;
        String letter = seatNumberAndLetter[1].toUpperCase();

        if (!List.of("A", "B", "C", "D", "E", "F").contains(letter)) {
            return false;
        }

        try {
            number = parseInt(seatNumberAndLetter[0]);
        } catch (NumberFormatException e) {
            return false;
        }

        var aircraftCapacity = flight.getAircraft();
        var ticketClassSeatRowNumberStart = 0;
        var ticketClassSeatRowNumberEnd = 0;

        switch (ticketClass) {
            case Business:
                ticketClassSeatRowNumberStart = 1;
                ticketClassSeatRowNumberEnd = aircraftCapacity.getBusinessCapacity();
                break;
            case Premium:
                ticketClassSeatRowNumberStart = aircraftCapacity.getBusinessCapacity() + 1;
                ticketClassSeatRowNumberEnd = aircraftCapacity.getPremiumCapacity();
                break;
            case Economy:
                ticketClassSeatRowNumberStart = aircraftCapacity.getPremiumCapacity() + 1;
                ticketClassSeatRowNumberEnd = aircraftCapacity.getEconomyCapacity();
                break;
        }

        return number >= ticketClassSeatRowNumberStart && number <= ticketClassSeatRowNumberEnd;
    }
}
