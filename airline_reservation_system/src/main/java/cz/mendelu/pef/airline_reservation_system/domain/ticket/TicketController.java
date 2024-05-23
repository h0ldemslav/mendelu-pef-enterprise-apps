package cz.mendelu.pef.airline_reservation_system.domain.ticket;

import cz.mendelu.pef.airline_reservation_system.domain.customer.Customer;
import cz.mendelu.pef.airline_reservation_system.domain.customer.CustomerService;
import cz.mendelu.pef.airline_reservation_system.domain.flight.Flight;
import cz.mendelu.pef.airline_reservation_system.domain.flight.FlightService;
import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.*;
import cz.mendelu.pef.airline_reservation_system.utils.helpers.ApiErrorDetails;
import cz.mendelu.pef.airline_reservation_system.utils.response.ArrayResponse;
import cz.mendelu.pef.airline_reservation_system.utils.response.ObjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Get all tickets")
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

    @Operation(summary = "Get one ticket by id")
    @GetMapping(value = "/{id}", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Id not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            )
    })
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

    @Operation(summary = "Create new ticket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Flight/customer not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "No available seat",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid flight (something is absent, e.g. aircraft or airports)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Customer has not enough credit",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            )
    })
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
        Ticket ticket = new Ticket();
        request.toTicket(ticket, flight, customer);

        try {
            ticketService.assignSeatNumber(flight, ticket);
        } catch (SeatIsNotAvailableException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No available seat", e);
        } catch (InvalidFlightException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid flight", e);
        } catch (NotEnoughCreditException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Customer has not enough credit", e);
        }

        ticketService.createTicket(ticket);
        customerService.updateCustomer(customer.getId(), customer);

        return ObjectResponse.of(
                ticket,
                TicketResponse::new
        );
    }

    @Operation(summary = "Change ticket seat number by id")
    @PutMapping(value = "/{id}/change_seat_number", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Id not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "No available seat",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid flight (something is absent, e.g. aircraft or airports)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Customer has not enough credit",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            )
    })
    @Valid
    public ObjectResponse<TicketResponse> changeSeatNumber(
            @PathVariable Long id,
            @Parameter(
                    description = "Valid seat number, integer and letter from [\"A\", \"B\", \"C\", \"D\", \"E\", \"F\"]",
                    example = "1A"
            )
            @RequestParam(name = "seat_number") String seatNumber
    ) {
        Ticket ticket = ticketService
                .getTicketById(id)
                .orElseThrow(NotFoundException::new);
        Customer customer = ticket.getCustomer();

        try {
            ticketService.changeSeatNumber(ticket, seatNumber);
        } catch (SeatIsNotAvailableException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No available seat", e);
        } catch (InvalidFlightException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid flight", e);
        } catch (NotEnoughCreditException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Customer has not enough credit", e);
        }

        customerService.updateCustomer(customer.getId(), customer);
        ticketService.updateTicket(id, ticket);

        return ObjectResponse.of(
                ticket,
                TicketResponse::new
        );
    }

    @Operation(summary = "Upgrade ticket class by id")
    @PutMapping(value = "/{id}/upgrade_ticket_class", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Id not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Invalid ticket class",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "No available seat",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Customer has not enough credit",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            )
    })
    @Valid
    public ObjectResponse<TicketResponse> upgradeTicketClass(
            @PathVariable Long id,
            @RequestParam(name = "new_ticket_class") TicketClass newTicketClass
    ) {
        Ticket ticket = ticketService
                .getTicketById(id)
                .orElseThrow(NotFoundException::new);
        Customer customer = ticket.getCustomer();

        try {
            ticketService.upgradeTicketClass(ticket, newTicketClass);
        } catch (InvalidTicketClassException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getDetail(), e);
        } catch (SeatIsNotAvailableException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No available seat", e);
        } catch (NotEnoughCreditException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Customer has not enough credit", e);
        }

        customerService.updateCustomer(customer.getId(), customer);
        ticketService.updateTicket(id, ticket);

        return ObjectResponse.of(
                ticket,
                TicketResponse::new
        );
    }

    @Operation(summary = "Transfer ticket from one flight to another by id")
    @PutMapping(value = "/{id}/transfer", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ticket or new flight not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid transfer information",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "No available seat",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid flight (something is absent, e.g. aircraft or airports)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Customer has not enough credit",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            )
    })
    @Valid
    public ObjectResponse<TicketResponse> transferTicketToOtherFlight(
            @PathVariable Long id,
            @RequestParam(name = "flight_id") Long flightId
    ) {
        Ticket ticket = ticketService
                .getTicketById(id)
                .orElseThrow(NotFoundException::new);
        Flight newFlight = flightService
                .getFlightById(flightId)
                .orElseThrow(NotFoundException::new);

        try {
            ticketService.transferTicketToOtherFlight(ticket, newFlight);
        } catch (InvalidTransferInformationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getDetail(), e);
        } catch (SeatIsNotAvailableException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No available seat", e);
        } catch (InvalidFlightException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid flight", e);
        } catch (NotEnoughCreditException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Customer has not enough credit", e);
        }

        Customer customer = ticket.getCustomer();

        customerService.updateCustomer(customer.getId(), customer);
        ticketService.updateTicket(id, ticket);

        return ObjectResponse.of(
                ticket,
                TicketResponse::new
        );
    }

    @Operation(summary = "Delete one ticket by id")
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTicketById(@PathVariable Long id) {
        ticketService.deleteTicketById(id);
    }
}
