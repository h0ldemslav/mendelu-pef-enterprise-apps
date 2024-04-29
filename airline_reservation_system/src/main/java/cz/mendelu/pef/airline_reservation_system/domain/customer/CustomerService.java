package cz.mendelu.pef.airline_reservation_system.domain.customer;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerService {

//    private final PasswordEncoder passwordEncoder;
    private CustomerRepository customerRepository;

    CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
//        this.passwordEncoder = passwordEncoder;
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
        // TODO: generate hash from password
        // var password = passwordEncoder.encode(customer.getPassword());
        // customer.setPassword(password);

        return customerRepository.save(customer);
    }

    public Customer updateCustomer(UUID id, Customer customer) {
        customer.setId(id);
        return customerRepository.save(customer);
    }

    public void deleteCustomerById(UUID id) {
        customerRepository.deleteById(id);
    }
}
