package ru.practicum.shareit.booking.strategy;

import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingStateStrategy {
    List<Booking> findBookings(Long userId, BookingRepository bookingRepository, LocalDateTime now);
}