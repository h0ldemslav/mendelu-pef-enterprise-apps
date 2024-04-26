package cz.mendelu.pef.airline_reservation_system.utils.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Function;

@Getter
@AllArgsConstructor
public class ArrayResponse<T> {

    List<T> items;

    static public <I, T> ArrayResponse<T> of(List<I> items, Function<I, T> mapper) {
        List<T> responses = items.stream()
                .map(mapper)
                .toList();
        return new ArrayResponse<>(responses, responses.size());
    }

    int count;
}