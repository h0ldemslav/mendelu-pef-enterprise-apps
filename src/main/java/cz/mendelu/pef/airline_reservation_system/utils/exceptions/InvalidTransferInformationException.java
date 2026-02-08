package cz.mendelu.pef.airline_reservation_system.utils.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InvalidTransferInformationException extends RuntimeException {
    private String detail;
}