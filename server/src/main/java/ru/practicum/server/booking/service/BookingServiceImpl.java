package ru.practicum.server.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.server.booking.BookingState;
import ru.practicum.server.booking.BookingStatus;
import ru.practicum.server.booking.dto.BookingDto;
import ru.practicum.server.booking.dto.BookingResponseDto;
import ru.practicum.server.booking.model.Booking;
import ru.practicum.server.booking.repository.BookingRepository;
import ru.practicum.server.booking.strategy.BookingStrategyFactory;
import ru.practicum.server.booking.strategy.BookingStateStrategy;
import ru.practicum.server.exception.IllegalArgumentException;
import ru.practicum.server.exception.NotFoundException;
import ru.practicum.server.exception.SecurityException;
import ru.practicum.server.item.model.Item;
import ru.practicum.server.item.repository.ItemRepository;
import ru.practicum.server.user.model.User;
import ru.practicum.server.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingStrategyFactory bookingStrategyFactory;

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingDto bookingDto, Long userId) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + bookingDto.getItemId() + " не найдена."));

        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования.");
        }

        if (item.getOwner().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь.");
        }

        if (bookingDto.getEnd().isBefore(bookingDto.getStart()) ||
                bookingDto.getEnd().equals(bookingDto.getStart())) {
            throw new IllegalArgumentException("Дата окончания должна быть позже даты начала.");
        }

        Booking booking = Booking.builder()
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        return BookingResponseDto.toDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto updateBookingStatus(Long bookingId, Boolean approved, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID " + bookingId + " не найдено."));

        if (!booking.getItem().getOwner().equals(userId)) {
            throw new SecurityException("Только владелец вещи может подтвердить или отклонить бронирование.");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException("Бронирование уже обработано.");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        return BookingResponseDto.toDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID " + bookingId + " не найдено."));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().equals(userId)) {
            throw new SecurityException("Только автор бронирования или владелец вещи может просмотреть бронирование.");
        }

        return BookingResponseDto.toDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String state, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(Sort.Direction.DESC, "start"));

        BookingState bookingState = parseBookingState(state);

        BookingStateStrategy strategy = bookingStrategyFactory.getUserStrategy(bookingState);

        List<Booking> bookings = strategy.findBookings(userId, bookingRepository, LocalDateTime.now(), pageable);

        return bookings.stream()
                .map(BookingResponseDto::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, String state, Integer from, Integer size) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + ownerId + " не найден."));

        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(Sort.Direction.DESC, "start"));

        BookingState bookingState = parseBookingState(state);

        BookingStateStrategy strategy = bookingStrategyFactory.getOwnerStrategy(bookingState);

        List<Booking> bookings = strategy.findBookings(ownerId, bookingRepository, LocalDateTime.now(), pageable);

        return bookings.stream()
                .map(BookingResponseDto::toDto)
                .collect(Collectors.toList());
    }

    private BookingState parseBookingState(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
    }
}