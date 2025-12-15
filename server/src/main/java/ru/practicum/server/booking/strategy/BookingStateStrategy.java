package ru.practicum.server.booking.strategy;

import org.springframework.data.domain.Pageable;
import ru.practicum.server.booking.BookingState;
import ru.practicum.server.booking.model.Booking;
import ru.practicum.server.booking.repository.BookingRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingStateStrategy {
    BookingState getState();

    boolean isForOwner();

    List<Booking> findBookings(Long userId, BookingRepository bookingRepository,
                               LocalDateTime now, Pageable pageable);
}