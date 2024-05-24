package cz.mendelu.pef.airline_reservation_system.domain.ticket;

import cz.mendelu.pef.airline_reservation_system.domain.customer.Customer;
import cz.mendelu.pef.airline_reservation_system.domain.customer.CustomerService;
import cz.mendelu.pef.airline_reservation_system.domain.flight.Flight;
import cz.mendelu.pef.airline_reservation_system.domain.flight.FlightService;
import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.InvalidTicketClassException;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.InvalidTransferInformationException;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.InvalidFlightException;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.SeatIsNotAvailableException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TicketService {

    private TicketRepository ticketRepository;
    private FlightService flightService;
    private CustomerService customerService;

    public TicketService(TicketRepository ticketRepository, FlightService flightService, CustomerService customerService) {
        this.ticketRepository = ticketRepository;
        this.flightService = flightService;
        this.customerService = customerService;
    }

    public List<Ticket> getAllTickets(Pageable pageRequest) {
        return ticketRepository
                .findAll(pageRequest)
                .getContent();
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    public Ticket createTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public Ticket updateTicket(Long id, Ticket ticket) {
        ticket.setId(id);
        return ticketRepository.save(ticket);
    }

    public void deleteTicketById(Long id) {
        ticketRepository.deleteById(id);
    }

    public Optional<Double> getTicketExtraPriceForCustomSeat(Ticket ticket) {
        Flight flight = ticket.getFlight();
        if (flight == null) {
            return Optional.empty();
        }

        return Optional.of(flight.getFareTariff().getPriceByTicketClass(ticket.getTicketClass()) * 0.1);
    }

    public void assignSeatNumber(Flight flight, Ticket ticket) {
        if (flight == null) {
            throw new InvalidFlightException();
        }

        TicketClass ticketClass = ticket.getTicketClass();
        var ticketPrice = flight.getFareTariff().getPriceByTicketClass(ticketClass);
        var seatNumber = ticket.getSeatNumber();

        if (seatNumber == null) {
            // Automatically assign the first available seat, if so
            final String newSeatNumber = flightService.issueSeatNumber(flight, ticketClass)
                    .orElseThrow(SeatIsNotAvailableException::new);
            ticket.setSeatNumber(newSeatNumber);
        } else {
            // Customer selected a custom seat that needs to be validated before setting
            var isSeatNumberValid = flightService.isSeatNumberValid(flight, ticketClass, seatNumber);
            var isSeatNumberOccupied = flightService.isSeatNumberOccupied(flight, seatNumber);

            if (!isSeatNumberValid || isSeatNumberOccupied) {
                throw new SeatIsNotAvailableException();
            }

            ticketPrice += getTicketExtraPriceForCustomSeat(ticket)
                    .orElseThrow(InvalidFlightException::new);
        }

        ticket.setPrice(ticketPrice);
        ticket.setDiscount(0.0);
        ticket.setPriceAfterDiscount(ticketPrice);

        customerService.chargeCustomerCredit(ticket.getCustomer(), ticketPrice);
    }

    public void changeSeatNumber(Ticket ticket, String seatNumber) {
        var flight = ticket.getFlight();
        var ticketClass = ticket.getTicketClass();
        var isSeatNumberValid = flightService.isSeatNumberValid(flight, ticketClass, seatNumber);
        var isSeatNumberOccupied = flightService.isSeatNumberOccupied(flight, seatNumber);

        if (!isSeatNumberValid || isSeatNumberOccupied) {
            throw new SeatIsNotAvailableException();
        }

        final double priceForSeatChange = getTicketExtraPriceForCustomSeat(ticket)
                .orElseThrow(InvalidFlightException::new);
        final double updatedTicketPrice = ticket.getPrice() + priceForSeatChange;

        customerService.chargeCustomerCredit(ticket.getCustomer(), priceForSeatChange);

        ticket.setPrice(updatedTicketPrice);
        ticket.setPriceAfterDiscount(updatedTicketPrice);
        ticket.setSeatNumber(seatNumber);
    }

    public boolean isTicketClassUpgradeValid(TicketClass newTicketClass, TicketClass oldTicketClass) {
        if (newTicketClass.equals(oldTicketClass)) {
            return false;
        }

        switch (oldTicketClass) {
            case Business:
                // Business is the highest class, so one cannot upgrade anymore
                return false;
            case Premium:
                // Downgrade is not possible
                if (newTicketClass == TicketClass.Economy) {
                    return false;
                }
        }

        return true;
    }

    public void upgradeTicketClass(Ticket ticket, TicketClass newTicketClass) {
        var oldTicketClass = ticket.getTicketClass();

        if (!isTicketClassUpgradeValid(newTicketClass, oldTicketClass)) {
            throw new InvalidTicketClassException("Ticket class must not be lower than or equal to the current ticket class.");
        }

        var flight = ticket.getFlight();
        var isTicketClassSeatsAvailable = flightService.isTicketClassSeatsAvailable(flight, newTicketClass);

        if (!isTicketClassSeatsAvailable) {
            throw new SeatIsNotAvailableException();
        }

        final String newSeatNumber = flightService
                .issueSeatNumber(flight, newTicketClass)
                // This exception should NOT happen, since seat availability was validated before
                .orElseThrow(SeatIsNotAvailableException::new);
        ticket.setSeatNumber(newSeatNumber);

        final double priceForTicketClassUpgrade = flight.getFareTariff().getPriceByTicketClass(newTicketClass) - ticket.getPrice();
        final double updatedTicketPrice = ticket.getPrice() + priceForTicketClassUpgrade;

        customerService.chargeCustomerCredit(ticket.getCustomer(), priceForTicketClassUpgrade);

        ticket.setPrice(updatedTicketPrice);
        ticket.setPriceAfterDiscount(updatedTicketPrice);
        ticket.setTicketClass(newTicketClass);
    }

    public Ticket transferTicketToOtherFlight(Ticket ticket, Flight newFlight) {
        String invalidTransferInformationDetail = "";

        if (ticket == null) {
            invalidTransferInformationDetail = "Missing ticket";
        } else if (ticket.getFlight() == null || newFlight == null) {
            invalidTransferInformationDetail = "Missing flights";
        } else if (ticket.getCustomer() == null) {
            invalidTransferInformationDetail = "Missing ticket customer";
        }

        if (!invalidTransferInformationDetail.isEmpty()) {
            throw new InvalidTransferInformationException(invalidTransferInformationDetail);
        }

        Flight oldTicketFlight = ticket.getFlight();

        if (Objects.equals(newFlight.getId(), oldTicketFlight.getId())) {
            invalidTransferInformationDetail = "Flights cannot be the same";
            throw new InvalidTransferInformationException(invalidTransferInformationDetail);
        }

        ticket.setSeatNumber(null);
        assignSeatNumber(newFlight, ticket);

        ticket.setFlight(newFlight);
        ticket.setDeparture(newFlight.getDeparture());
        ticket.setArrival(newFlight.getArrival());

        return ticket;
    }
}
