package ru.practicum.server.booking.strategy.owner;

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
public class OwnerRejectedBookingStateStrategy implements BookingStateStrategy {

    @Override
    public BookingState getState() {
        return BookingState.REJECTED;
    }

    @Override
    public boolean isForOwner() {
        return true;
    }

    @Override
    public List<Booking> findBookings(Long ownerId, BookingRepository bookingRepository,
                                      LocalDateTime now, Pageable pageable) {
        return bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, pageable);
    }
}