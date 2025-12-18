package ru.practicum.shareit.booking.strategy.owner;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.strategy.BookingStateStrategy;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OwnerPastBookingStateStrategy implements BookingStateStrategy {

    @Override
    public BookingState getState() {
        return BookingState.PAST;
    }

    @Override
    public boolean isForOwner() {
        return true;
    }

    @Override
    public List<Booking> findBookings(Long ownerId, BookingRepository bookingRepository,
                                      LocalDateTime now, Pageable pageable) {
        return bookingRepository.findPastBookingsByItemOwnerId(ownerId, now, pageable);
    }
}