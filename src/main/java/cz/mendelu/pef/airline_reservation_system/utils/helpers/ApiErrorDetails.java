package cz.mendelu.pef.airline_reservation_system.utils.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiErrorDetails {

    private long timestamp;

    private int status;

    private String error;

    private String exception;

    private String message;

    private String path;
}
