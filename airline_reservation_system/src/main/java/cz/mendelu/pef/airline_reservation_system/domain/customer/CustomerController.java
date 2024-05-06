package cz.mendelu.pef.airline_reservation_system.domain.customer;

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

import java.util.UUID;

@RestController
@RequestMapping("customers")
@Validated
public class CustomerController {

    private CustomerService customerService;

    @Autowired
    CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping(value = "", produces = "application/json")
    @Valid
    public ArrayResponse<CustomerResponse> getCustomers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit
    ) {
        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = limit != null && limit > 0 ? limit : 100;
        Pageable pageRequest = PageRequest.of(pageNumber, pageSize);

        return ArrayResponse.of(
                customerService.getAllCustomers(pageRequest),
                CustomerResponse::new
        );
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @Valid
    public ObjectResponse<CustomerResponse> getCustomerById(@PathVariable UUID id) {
        Customer customer = customerService
                .getCustomerById(id)
                .orElseThrow(NotFoundException::new);

        return ObjectResponse.of(
                customer,
                CustomerResponse::new
        );
    }

    @PostMapping(value = "", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @Valid
    public ObjectResponse<CustomerResponse> createCustomer(@RequestBody @Valid CustomerRequest request) {
        Customer customer = new Customer();
        request.toCustomer(customer);

        customerService.createCustomer(customer);

        return ObjectResponse.of(
                customer,
                CustomerResponse::new
        );
    }

    @PutMapping(value = "/{id}", produces = "application/json")
    @Valid
    public ObjectResponse<CustomerResponse> updateCustomerById(
            @PathVariable UUID id,
            @RequestBody @Valid CustomerRequest request
    ) {
        Customer customer = customerService
                .getCustomerById(id)
                .orElseThrow(NotFoundException::new);
        request.toCustomer(customer);

        customerService.updateCustomer(id, customer);

        return ObjectResponse.of(
                customer,
                CustomerResponse::new
        );
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomerById(@PathVariable UUID id) {
        customerService.deleteCustomerById(id);
    }
}
