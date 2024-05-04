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

        if (!flightService.isTicketClassSeatsAvailable(flight, ticketClass)) {
            ticket.detachFromRelatedEntities();
            throw new SeatIsNotAvailableException();
        }

        var ticketPrice = ticket.getPrice();

        if (ticket.getSeatNumber() == null) {
            // Automatically assign the closest available seat
            ticket.setSeatNumber(flightService.getSeatNumber(flight, ticketClass));
        } else {
            // Customer selected a custom seat that needs to be validated before assigning
            var isCustomSeatNumberValid = flightService.isSeatNumberValid(flight, ticketClass, request.getSeatNumber());
            var isSeatNumberOccupied = flightService.isSeatNumberOccupied(flight, request.getSeatNumber().trim());

            if (!isCustomSeatNumberValid || isSeatNumberOccupied) {
                ticket.detachFromRelatedEntities();
                throw new SeatIsNotAvailableException();
            }

            ticketPrice += ticketService.getTicketExtraPriceForCustomSeat(ticket, ticketClass);
        }

        if (!customerService.isCustomerHasEnoughCredit(customer, ticketPrice)) {
            ticket.detachFromRelatedEntities();
            throw new NotEnoughCreditException();
        }

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

    @PutMapping(value = "/{id}", produces = "application/json")
    @Valid
    public ObjectResponse<TicketResponse> updateTicketById(
            @PathVariable Long id,
            @RequestBody @Valid TicketRequest request
    ) {
        Customer customer = customerService
                .getCustomerById(request.getCustomerId())
                .orElseThrow(NotFoundException::new);
        Flight flight = flightService
                .getFlightById(request.getFlightId())
                .orElseThrow(NotFoundException::new);
        TicketClass ticketClass = TicketClass
                .getTicketClassByString(request.getTicketClass())
                .orElseThrow(BadRequestException::new);
        Ticket ticket = ticketService
                .getTicketById(id)
                .orElseThrow(NotFoundException::new);
        request.toTicket(ticket, ticketClass, flight, customer);

        ticketService.updateTicket(id, ticket);

        return ObjectResponse.of(
                ticket,
                TicketResponse::new
        );
    }

    @PutMapping(value = "/{id}/reassign_seat", produces = "application/json")
    public ObjectResponse<TicketResponse> changeSeatAssignment(@PathVariable Long id, @RequestParam String seatNumber) {
        Ticket ticket = ticketService
                .getTicketById(id)
                .orElseThrow(NotFoundException::new);
        Customer customer = ticket.getCustomer();
        Flight flight = ticket.getFlight();
        if (flight == null) {
            throw new BadRequestException();
        }

        TicketClass ticketClass = TicketClass
                .getTicketClassByString(ticket.getTicketClass())
                .orElseThrow(BadRequestException::new);

        if (!flightService.isSeatNumberValid(flight, ticketClass, seatNumber)) {
            throw new BadRequestException();
        }

        if (flightService.isSeatNumberOccupied(flight, seatNumber)) {
            throw new SeatIsNotAvailableException();
        }

        var priceForSeatReassignment = ticketService.getTicketExtraPriceForCustomSeat(ticket, ticketClass);
        var isCustomerCreditEnoughForSeat = customerService.isCustomerHasEnoughCredit(
                customer,
                priceForSeatReassignment
        );

        if (!isCustomerCreditEnoughForSeat) {
            throw new NotEnoughCreditException();
        }

        customer.setCredit(customer.getCredit() - priceForSeatReassignment);
        ticket.setPrice(ticket.getPrice() + priceForSeatReassignment);
        ticket.setPriceAfterDiscount(ticket.getPrice());
        ticket.setSeatNumber(seatNumber);

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
}
