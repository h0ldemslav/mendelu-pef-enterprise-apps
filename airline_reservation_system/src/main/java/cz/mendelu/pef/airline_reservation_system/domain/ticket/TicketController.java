package cz.mendelu.pef.airline_reservation_system.domain.ticket;

import cz.mendelu.pef.airline_reservation_system.domain.customer.CustomerService;
import cz.mendelu.pef.airline_reservation_system.domain.flight.FlightService;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.NotFoundException;
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
        // TODO: check if customer has money
        Ticket ticket = new Ticket();
        request.toTicket(ticket, customerService, flightService);

        ticketService.createTicket(ticket);

        return ObjectResponse.of(
                ticket,
                TicketResponse::new
        );
    }

}
