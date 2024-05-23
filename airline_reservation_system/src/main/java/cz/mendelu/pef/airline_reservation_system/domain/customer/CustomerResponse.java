package cz.mendelu.pef.airline_reservation_system.domain.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CustomerResponse {

    private UUID id;

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
