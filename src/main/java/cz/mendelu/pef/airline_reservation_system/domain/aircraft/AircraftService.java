package cz.mendelu.pef.airline_reservation_system.domain.aircraft;

import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AircraftService {

    private AircraftRepository aircraftRepository;

    public AircraftService(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
    }

    public List<Aircraft> getAllAircrafts() {
        List<Aircraft> aircrafts = new ArrayList<>();
        aircraftRepository.findAll().forEach(aircrafts::add);

        return aircrafts;
    }

    public Optional<Aircraft> getAircraftById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return aircraftRepository.findById(id);
    }

    public Aircraft createAircraft(Aircraft aircraft) {
        return aircraftRepository.save(aircraft);
    }

    public Aircraft updateAircraft(Long id, Aircraft aircraft) {
        aircraft.setId(id);
        return aircraftRepository.save(aircraft);
    }

    public void deleteAircraftById(Long id) {
        aircraftRepository.deleteById(id);
    }

    /**
     * Calculates start and end seat row numbers for a passed aircraft and ticket class.
     * @return array containing start (first element) and end (second element) seat row numbers
     */
    public int[] getStartAndEndSeatRowNumbers(Aircraft aircraft, TicketClass ticketClass, int allowedSeatLettersSize) {
        var ticketClassSeatRowNumberStart = 0;
        var ticketClassSeatRowNumberEnd = 0;

        switch (ticketClass) {
            case Business:
                ticketClassSeatRowNumberStart = 1;
                ticketClassSeatRowNumberEnd = aircraft.getTotalNumberOfSeatRows(TicketClass.Business, allowedSeatLettersSize);

                break;
            case Premium:
                var premiumEnd = aircraft.getTotalNumberOfSeatRows(TicketClass.Premium, allowedSeatLettersSize);

                ticketClassSeatRowNumberStart = 1 + aircraft.getTotalNumberOfSeatRows(TicketClass.Business, allowedSeatLettersSize);
                ticketClassSeatRowNumberEnd = ticketClassSeatRowNumberStart + (premiumEnd - 1);

                break;
            case Economy:
                var economyEnd = aircraft.getTotalNumberOfSeatRows(TicketClass.Economy, allowedSeatLettersSize);

                ticketClassSeatRowNumberStart = 1 + aircraft.getTotalNumberOfSeatRows(TicketClass.Premium, allowedSeatLettersSize)
                        + aircraft.getTotalNumberOfSeatRows(TicketClass.Business, allowedSeatLettersSize);
                ticketClassSeatRowNumberEnd = ticketClassSeatRowNumberStart + (economyEnd - 1);

                break;
        }

        return new int[]{ ticketClassSeatRowNumberStart, ticketClassSeatRowNumberEnd };
    }
}
