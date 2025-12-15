package ru.practicum.shareit.booking.strategy.state;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.strategy.BookingStateStrategy;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PastBookingStateStrategy implements BookingStateStrategy {

    @Override
    public BookingState getState() {
        return BookingState.PAST;
    }

    @Override
    public boolean isForOwner() {
        return false;
    }

    @Override
    public List<Booking> findBookings(Long userId, BookingRepository bookingRepository,
                                      LocalDateTime now, Pageable pageable) {
        return bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now, pageable);
    }
}