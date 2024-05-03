package cz.mendelu.pef.airline_reservation_system.utils.exceptions;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
@AllArgsConstructor
public class NoAvailableSeatException extends RuntimeException {
}
