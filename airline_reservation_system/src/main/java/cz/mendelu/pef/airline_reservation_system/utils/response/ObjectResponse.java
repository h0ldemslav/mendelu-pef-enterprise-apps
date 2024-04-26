package cz.mendelu.pef.airline_reservation_system.utils.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public class ObjectResponse<T> {

    T content;

    static public <I, T> ObjectResponse<T> of(I obj, Function<I, T> mapper) {
        return new ObjectResponse<>(mapper.apply(obj));
    }
}
