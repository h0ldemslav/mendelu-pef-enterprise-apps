package cz.mendelu.pef.airline_reservation_system.domain.customer;

import cz.mendelu.pef.airline_reservation_system.domain.flight.Flight;
import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerService {

    private CustomerRepository customerRepository;

    CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
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

    /**
     *
     * @param flight a flight chosen by customer.
     * @param ticketClass a flight ticket class chosen by customer.
     * @param customSeatPrice an extra price that is used for calculating the total ticket price,
     *                        if customer selected a custom seat.
     * @param customer a customer that wants to purchase a ticket for the flight.
     * @return boolean that indicates if customer has enough credit to buy a ticket with that ticket class.
     */
    public boolean isEnoughCreditForFlightTicketClass(
            Flight flight,
            TicketClass ticketClass,
            double customSeatPrice,
            Customer customer
    ) {
        var customerCredit = customer.getCredit();
        var flightTicketPrice = flight.getFareTariff().getPriceByTicketClass(ticketClass);

        return customerCredit >= (flightTicketPrice + customSeatPrice);
    }
}
