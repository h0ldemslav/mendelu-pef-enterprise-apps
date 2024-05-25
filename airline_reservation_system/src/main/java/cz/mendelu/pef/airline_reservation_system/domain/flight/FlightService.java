package cz.mendelu.pef.airline_reservation_system.domain.flight;

import cz.mendelu.pef.airline_reservation_system.domain.aircraft.Aircraft;
import cz.mendelu.pef.airline_reservation_system.domain.aircraft.AircraftService;
import cz.mendelu.pef.airline_reservation_system.domain.fare_tariff.FareTariff;
import cz.mendelu.pef.airline_reservation_system.domain.ticket.Ticket;
import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.InvalidFlightException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.lang.Integer.parseInt;

@Service
public class FlightService {

    private FlightRepository flightRepository;

    private AircraftService aircraftService;

    public FlightService(FlightRepository flightRepository, AircraftService aircraftService) {
        this.flightRepository = flightRepository;
        this.aircraftService = aircraftService;
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

    public Map<String, List<String>> getAllSeats(Flight flight) {
        if (flight == null || flight.getAircraft() == null) {
            throw new InvalidFlightException("Invalid flight or flight doesn't have an assigned aircraft");
        }

        TicketClass[] allTicketClasses = TicketClass.values();
        Map<String, List<String>> ticketClassToSeatNumbers = new HashMap<>();

        for (TicketClass ticketClass : allTicketClasses) {
            ticketClassToSeatNumbers.put(ticketClass.name(), new ArrayList<>());
        }

        final Aircraft aircraft = flight.getAircraft();
        final List<String> allowedSeatLetters = List.of("A", "B", "C", "D", "E", "F");

        int seatRowNumber = 1;

        for (TicketClass currentTicketClass : allTicketClasses) {
            List<String> currentTicketClassSeatNumbers = ticketClassToSeatNumbers.get(currentTicketClass.name());
            int currentTicketClassCapacity = aircraft.getCapacityByTicketClass(currentTicketClass);

            for (int j = 0; j < allowedSeatLetters.size(); j++) {
                currentTicketClassSeatNumbers.add(seatRowNumber + allowedSeatLetters.get(j));

                // Avoid creating seat numbers that not fit into ticket class capacity
                if (currentTicketClassSeatNumbers.size() == currentTicketClassCapacity) {
                    seatRowNumber++;
                    break;
                }

                if (j == allowedSeatLetters.size() - 1) {
                    j = -1;
                    seatRowNumber++;
                }
            }
        }

        return ticketClassToSeatNumbers;
    }

    public Map<String, List<String>> getOccupiedSeats(Flight flight) {
        if (flight == null) {
            throw new InvalidFlightException("Invalid flight");
        }

        TicketClass[] allTicketClasses = TicketClass.values();
        Map<String, List<String>> ticketClassToSeatNumbers = new HashMap<>();

        for (TicketClass ticketClass : allTicketClasses) {
            ticketClassToSeatNumbers.put(ticketClass.name(), new ArrayList<>());
        }

        flight
                .getTickets()
                .stream()
                .filter(t -> t.getSeatNumber() != null)
                .forEach(t -> {
                    ticketClassToSeatNumbers
                            .get(t.getTicketClass().name())
                            .add(t.getSeatNumber());
                });

        return ticketClassToSeatNumbers;
    }

    public Map<String, List<String>> getAvailableSeats(Flight flight) {
        Map<String, List<String>> allSeats = getAllSeats(flight);
        Map<String, List<String>> occupiedSeats = getOccupiedSeats(flight);

        allSeats.forEach((ticketClassName, seatNumbers) -> {
            var availableSeats = seatNumbers
                    .stream()
                    .filter(sn -> !occupiedSeats.get(ticketClassName).contains(sn))
                    .toList();
            allSeats.put(ticketClassName, availableSeats);
        });

        return allSeats;
    }

    public Optional<String> issueSeatNumber(Flight flight, TicketClass ticketClass) {
        if (flight == null || flight.getAircraft() == null) {
            return Optional.empty();
        }

        if (!isTicketClassSeatsAvailable(flight, ticketClass)) {
            return Optional.empty();
        }

        return getAvailableSeats(flight)
                .get(ticketClass.name())
                .stream()
                .findFirst();
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
                .filter(t -> t.getId() != null && Objects.equals(t.getTicketClass().name(), ticketClass.name()))
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

        var startAndEndSeatRowNumbers = aircraftService
                .getStartAndEndSeatRowNumbers(flight.getAircraft(), ticketClass, allowedSeatLetters.size());

        return number >= startAndEndSeatRowNumbers[0] && number <= startAndEndSeatRowNumbers[1];
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

    public void cancelFlight(Flight flight, Double ticketDiscountPercentage) {
        if (flight != null) {
            flight.setStatus("Cancelled");

            if (ticketDiscountPercentage != null
                    && (ticketDiscountPercentage > 0.0 && ticketDiscountPercentage <= 100.0)) {
                // Apply discount due to flight cancellation
                var tickets = flight.getTickets();

                for (Ticket t : tickets) {
                    var ticketPrice = t.getPrice();
                    var discountPercentage = ticketDiscountPercentage / 100.0;
                    var discountAmount = ticketPrice * discountPercentage;

                    t.setDiscount(discountAmount);
                    t.setPriceAfterDiscount(ticketPrice - discountAmount);
                }
            }
        }
    }
}
