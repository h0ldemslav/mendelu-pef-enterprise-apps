package cz.mendelu.pef.airline_reservation_system.domain.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerRequest {

    @JsonProperty("first_name")
    @Schema(example = "John")
    @NotEmpty
    private String firstName;

    @JsonProperty("last_name")
    @Schema(example = "Doe")
    @NotEmpty
    private String lastName;

    @Schema(description = "Customer credit in dollars, 0 or greater", example = "100.00")
    @NotNull
    @Min(0)
    private Double credit;

    @Schema(example = "+1 999 999 99")
    @NotEmpty
    private String phone;

    @Schema(example = "johndoe2077@gmail.com")
    @NotEmpty
    private String email;

    @Schema(description = "Password hash", example = "$2a$04$3g9ACTIDCfgAy8kpjklEUu21yw10PoxXrDCYHKDzQ1yEwURf7gUci")
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
