package ru.practicum.server.booking.strategy.state;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.server.booking.BookingState;
import ru.practicum.server.booking.BookingStatus;
import ru.practicum.server.booking.model.Booking;
import ru.practicum.server.booking.repository.BookingRepository;
import ru.practicum.server.booking.strategy.BookingStateStrategy;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class WaitingBookingStateStrategy implements BookingStateStrategy {

    @Override
    public BookingState getState() {
        return BookingState.WAITING;
    }

    @Override
    public boolean isForOwner() {
        return false;
    }

    @Override
    public List<Booking> findBookings(Long userId, BookingRepository bookingRepository,
                                      LocalDateTime now, Pageable pageable) {
        return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING, pageable);
    }
}