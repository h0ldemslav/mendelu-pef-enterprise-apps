package cz.mendelu.pef.airline_reservation_system.domain.customer;

import cz.mendelu.pef.airline_reservation_system.domain.flight.Flight;
import cz.mendelu.pef.airline_reservation_system.domain.flight.FlightRepository;
import cz.mendelu.pef.airline_reservation_system.domain.ticket.Ticket;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.NotEnoughCreditException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private CustomerRepository customerRepository;

    private FlightRepository flightRepository;

    public CustomerService(CustomerRepository customerRepository, FlightRepository flightRepository) {
        this.customerRepository = customerRepository;
        this.flightRepository = flightRepository;
    }

    public List<Customer> getAllCustomers(Pageable pageRequest) {
        return customerRepository
                .findAll(pageRequest)
                .getContent();
    }

    public Optional<Customer> getCustomerById(UUID id) {
        return customerRepository.findById(id);
    }

    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(UUID id, Customer customer) {
        customer.setId(id);
        return customerRepository.save(customer);
    }

    public void deleteCustomerById(UUID id) {
        customerRepository.deleteById(id);
    }

    public boolean isCustomerHasEnoughCredit(Customer customer, double amountOfMoney) {
        return customer.getCredit() >= amountOfMoney;
    }

    public void chargeCustomerCredit(Customer customer, Double amountOfMoney) {
        if (!isCustomerHasEnoughCredit(customer, amountOfMoney)) {
            throw new NotEnoughCreditException();
        }

        customer.setCredit(customer.getCredit() - amountOfMoney);
    }

    public List<Flight> generateFlightRecommendations(Customer customer) {
        List<Flight> flights = customer.getPurchasedTickets()
                .stream()
                .map(Ticket::getFlight)
                .filter(Objects::nonNull)
                .toList();

        // By counting how many times a customer arrives at different airports,
        // it can be assumed that the customer has work/vacation at the destination,
        // so he/she travels to those airports frequently -> possible favorite destinations
        List<String> favoriteDestinations = flights
                .stream()
                .collect(Collectors.groupingBy(flight -> flight.getAirportArrival().getCode(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        return flightRepository
                .findAll()
                .stream()
                .filter(flight -> flight.getStatus().trim().equalsIgnoreCase("Scheduled")
                        && favoriteDestinations.contains(flight.getAirportArrival().getCode()))
                .sorted(Comparator.comparing(Flight::getDeparture))
                .toList();
    }
}
