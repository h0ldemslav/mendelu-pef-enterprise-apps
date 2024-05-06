package cz.mendelu.pef.airline_reservation_system.domain.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerRequest {

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

    public void toCustomer(Customer customer) {
        customer.setFirstName(this.firstName);
        customer.setLastName(this.lastName);
        customer.setCredit(this.credit);
        customer.setPhone(this.phone);
        customer.setEmail(this.email);
        customer.setPassword(this.password);
    }
}
