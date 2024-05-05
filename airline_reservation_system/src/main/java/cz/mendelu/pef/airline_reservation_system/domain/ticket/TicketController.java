package cz.mendelu.pef.airline_reservation_system.domain.ticket;

import cz.mendelu.pef.airline_reservation_system.domain.customer.Customer;
import cz.mendelu.pef.airline_reservation_system.domain.customer.CustomerService;
import cz.mendelu.pef.airline_reservation_system.domain.flight.Flight;
import cz.mendelu.pef.airline_reservation_system.domain.flight.FlightService;
import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.*;
import cz.mendelu.pef.airline_reservation_system.utils.response.ArrayResponse;
import cz.mendelu.pef.airline_reservation_system.utils.response.ObjectResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("tickets")
@Validated
public class TicketController {

    private TicketService ticketService;

    private FlightService flightService;

    private CustomerService customerService;

    @Autowired
    public TicketController(TicketService ticketService, FlightService flightService, CustomerService customerService) {
        this.ticketService = ticketService;
        this.flightService = flightService;
        this.customerService = customerService;
    }

    @GetMapping(value = "", produces = "application/json")
    @Valid
    public ArrayResponse<TicketResponse> getTickets(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit
    ) {
        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = limit != null && limit > 0 ? limit : 100;
        Pageable pageRequest = PageRequest.of(pageNumber, pageSize);

        return ArrayResponse.of(
                ticketService.getAllTickets(pageRequest),
                TicketResponse::new
        );
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @Valid
    public ObjectResponse<TicketResponse> getTicketById(@PathVariable Long id) {
        Ticket ticket = ticketService
                .getTicketById(id)
                .orElseThrow(NotFoundException::new);

        return ObjectResponse.of(
                ticket,
                TicketResponse::new
        );
    }

    @PostMapping(value = "", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @Valid
    public ObjectResponse<TicketResponse> createTicket(@RequestBody @Valid TicketRequest request) {
        Customer customer = customerService
                .getCustomerById(request.getCustomerId())
                .orElseThrow(NotFoundException::new);
        Flight flight = flightService
                .getFlightById(request.getFlightId())
                .orElseThrow(NotFoundException::new);
        TicketClass ticketClass = TicketClass
                .getTicketClassByString(request.getTicketClass())
                .orElseThrow(BadRequestException::new);
        Ticket ticket = new Ticket();
        request.toTicket(ticket, ticketClass, flight, customer);

        validateSeatsAvailability(flight, ticketClass, ticket);

        var ticketPrice = ticket.getPrice();
        var seatNumber = ticket.getSeatNumber();

        if (seatNumber == null) {
            // Automatically assign the closest available seat
            final String newSeatNumber = flightService
                    .getSeatNumber(flight, ticketClass)
                    // This exception should NOT happen, since seat availability was validated before
                    .orElseThrow(SeatIsNotAvailableException::new);
            ticket.setSeatNumber(newSeatNumber);
        } else {
            // Customer selected a custom seat that needs to be validated before assigning
            validateSeatNumber(flight, ticketClass, seatNumber);
            ticketPrice += ticketService
                    .getTicketExtraPriceForCustomSeat(ticket, ticketClass)
                    .orElseThrow(BadRequestException::new);
        }

        validateCustomerCredit(customer, ticket, ticketPrice);

        customer.setCredit(customer.getCredit() - ticketPrice);
        ticket.setPrice(ticketPrice);
        ticket.setPriceAfterDiscount(ticketPrice);

        ticketService.createTicket(ticket);
        customerService.updateCustomer(customer.getId(), customer);

        return ObjectResponse.of(
                ticket,
                TicketResponse::new
        );
    }

    @PutMapping(value = "/{id}/change_seat_number", produces = "application/json")
    @Valid
    public ObjectResponse<TicketResponse> changeSeatNumber(@PathVariable Long id, @RequestParam String seatNumber) {
        Ticket ticket = ticketService
                .getTicketById(id)
                .orElseThrow(NotFoundException::new);
        TicketClass ticketClass = TicketClass
                .getTicketClassByString(ticket.getTicketClass())
                .orElseThrow(BadRequestException::new);
        Customer customer = ticket.getCustomer();
        Flight flight = ticket.getFlight();

        validateSeatNumber(flight, ticketClass, seatNumber);

        final double priceForSeatReassignment = ticketService
                .getTicketExtraPriceForCustomSeat(ticket, ticketClass)
                .orElseThrow(BadRequestException::new);
        final double updatedTicketPrice = ticket.getPrice() + priceForSeatReassignment;

        validateCustomerCredit(customer, ticket, priceForSeatReassignment);

        customer.setCredit(customer.getCredit() - priceForSeatReassignment);
        ticket.setPrice(updatedTicketPrice);
        ticket.setPriceAfterDiscount(updatedTicketPrice);
        ticket.setSeatNumber(seatNumber);

        customerService.updateCustomer(customer.getId(), customer);
        ticketService.updateTicket(id, ticket);

        return ObjectResponse.of(
                ticket,
                TicketResponse::new
        );
    }

    @PutMapping(value = "/{id}/upgrade_ticket_class", produces = "application/json")
    @Valid
    public ObjectResponse<TicketResponse> upgradeTicketClass(
            @PathVariable Long id,
            @RequestParam TicketClass newTicketClass
    ) {
        Ticket ticket = ticketService
                .getTicketById(id)
                .orElseThrow(NotFoundException::new);
        TicketClass oldTicketClass = TicketClass
                .getTicketClassByString(ticket.getTicketClass())
                .orElseThrow(BadRequestException::new);
        Customer customer = ticket.getCustomer();
        Flight flight = ticket.getFlight();

        validateTicketClass(newTicketClass, oldTicketClass);
        validateSeatsAvailability(flight, newTicketClass, ticket);

        final double priceForTicketClassUpgrade = flight.getFareTariff().getPriceByTicketClass(newTicketClass) - ticket.getPrice();
        final double updatedTicketPrice = ticket.getPrice() + priceForTicketClassUpgrade;

        validateCustomerCredit(customer, ticket, priceForTicketClassUpgrade);
        customer.setCredit(customer.getCredit() - priceForTicketClassUpgrade);

        ticket.setPrice(updatedTicketPrice);
        ticket.setPriceAfterDiscount(updatedTicketPrice);
        ticket.setTicketClass(newTicketClass.name());

        final String newSeatNumber = flightService
                .getSeatNumber(flight, newTicketClass)
                // This exception should NOT happen, since seat availability was validated before
                .orElseThrow(SeatIsNotAvailableException::new);
        ticket.setSeatNumber(newSeatNumber);

        customerService.updateCustomer(customer.getId(), customer);
        ticketService.updateTicket(id, ticket);

        return ObjectResponse.of(
                ticket,
                TicketResponse::new
        );
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTicketById(@PathVariable Long id) {
        ticketService.deleteTicketById(id);
    }

    private void validateSeatsAvailability(Flight flight, TicketClass ticketClass, Ticket ticket) throws SeatIsNotAvailableException {
        if (!flightService.isTicketClassSeatsAvailable(flight, ticketClass)) {
            ticket.detachFromRelatedEntities();
            throw new SeatIsNotAvailableException();
        }
    }
    
    private void validateSeatNumber(Flight flight, TicketClass ticketClass, String seatNumber) throws SeatIsNotAvailableException {
        var seatNumberTrimmed = seatNumber.trim();
        var isSeatNumberValid = flightService.isSeatNumberValid(flight, ticketClass, seatNumberTrimmed);
        var isSeatNumberOccupied = flightService.isSeatNumberOccupied(flight, seatNumberTrimmed);

        if (!isSeatNumberValid || isSeatNumberOccupied) {
            throw new SeatIsNotAvailableException();
        }
    }

    private void validateCustomerCredit(Customer customer, Ticket ticket, double ticketPrice) {
        if (!customerService.isCustomerHasEnoughCredit(customer, ticketPrice)) {
            ticket.detachFromRelatedEntities();
            throw new NotEnoughCreditException();
        }
    }

    private void validateTicketClass(TicketClass newTicketClass, TicketClass oldTicketClass) {
        var conflictException = new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ticket class must not be lower than or equal to the current ticket class."
        );

        if (newTicketClass.equals(oldTicketClass)) {
            throw conflictException;
        }

        switch (oldTicketClass) {
            case Business:
                // Business is the highest class, so one cannot upgrade anymore
                throw conflictException;
            case Premium:
                // Downgrade is not possible
                if (newTicketClass == TicketClass.Economy) {
                    throw conflictException;
                }
        }
    }
}
