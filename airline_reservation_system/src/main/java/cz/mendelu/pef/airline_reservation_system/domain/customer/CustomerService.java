package cz.mendelu.pef.airline_reservation_system.domain.customer;

import cz.mendelu.pef.airline_reservation_system.utils.exceptions.NotEnoughCreditException;
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

    public boolean isCustomerHasEnoughCredit(Customer customer, double amountOfMoney) {
        return customer.getCredit() >= amountOfMoney;
    }

    public void chargeCustomerCredit(Customer customer, Double amountOfMoney) {
        if (!isCustomerHasEnoughCredit(customer, amountOfMoney)) {
            throw new NotEnoughCreditException();
        }

        customer.setCredit(customer.getCredit() - amountOfMoney);
    }
}
