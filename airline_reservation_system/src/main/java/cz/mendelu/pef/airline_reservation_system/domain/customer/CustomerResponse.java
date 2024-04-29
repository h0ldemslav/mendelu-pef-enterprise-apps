package cz.mendelu.pef.airline_reservation_system.domain.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CustomerResponse {

    private UUID id;

    @JsonProperty("first_name")
    @NotEmpty
    private String firstName;

    @JsonProperty("last_name")
    @NotEmpty
    private String lastName;

    @NotNull
    @Min(0)
    private Double credit;

    @NotEmpty
    private String phone;

    @NotEmpty
    private String email;

    @NotEmpty
    private String password;

    CustomerResponse(Customer customer) {
        this.id = customer.getId();
        this.firstName = customer.getFirstName();
        this.lastName = customer.getLastName();
        this.credit = customer.getCredit();
        this.phone = customer.getPhone();
        this.email = customer.getEmail();
        this.password = customer.getPassword();
    }
}
