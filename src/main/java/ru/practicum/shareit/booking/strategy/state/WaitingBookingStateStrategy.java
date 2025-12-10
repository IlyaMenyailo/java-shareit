package ru.practicum.shareit.booking.strategy.state;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.strategy.BookingStateStrategy;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class WaitingBookingStateStrategy implements BookingStateStrategy {
    private final Pageable pageable;

    @Override
    public List<Booking> findBookings(Long userId, BookingRepository bookingRepository, LocalDateTime now) {
        return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING, pageable);
    }
}