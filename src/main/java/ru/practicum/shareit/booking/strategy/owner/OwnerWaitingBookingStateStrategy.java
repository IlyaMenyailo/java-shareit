package ru.practicum.shareit.booking.strategy.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.strategy.BookingStateStrategy;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class OwnerWaitingBookingStateStrategy implements BookingStateStrategy {
    private final Pageable pageable;

    @Override
    public List<Booking> findBookings(Long ownerId, BookingRepository bookingRepository, LocalDateTime now) {
        return bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING, pageable);
    }
}