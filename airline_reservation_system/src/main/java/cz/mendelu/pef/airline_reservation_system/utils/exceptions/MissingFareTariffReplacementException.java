package cz.mendelu.pef.airline_reservation_system.utils.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class MissingFareTariffReplacementException extends RuntimeException {
}
