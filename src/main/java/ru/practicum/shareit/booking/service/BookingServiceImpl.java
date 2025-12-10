package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.IllegalArgumentException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.SecurityException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

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

        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }

        switch (bookingState) {
            case ALL:
                return bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable)
                        .stream()
                        .map(BookingResponseDto::toDto)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findCurrentBookingsByBookerId(userId, LocalDateTime.now(), pageable)
                        .stream()
                        .map(BookingResponseDto::toDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(
                                userId, LocalDateTime.now(), pageable)
                        .stream()
                        .map(BookingResponseDto::toDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(
                                userId, LocalDateTime.now(), pageable)
                        .stream()
                        .map(BookingResponseDto::toDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                                userId, BookingStatus.WAITING, pageable)
                        .stream()
                        .map(BookingResponseDto::toDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                                userId, BookingStatus.REJECTED, pageable)
                        .stream()
                        .map(BookingResponseDto::toDto)
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, String state, Integer from, Integer size) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + ownerId + " не найден."));

        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(Sort.Direction.DESC, "start"));

        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }

        switch (bookingState) {
            case ALL:
                return bookingRepository.findByItemOwnerId(ownerId, pageable)
                        .stream()
                        .map(BookingResponseDto::toDto)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findCurrentBookingsByItemOwnerId(ownerId, LocalDateTime.now(), pageable)
                        .stream()
                        .map(BookingResponseDto::toDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findPastBookingsByItemOwnerId(ownerId, LocalDateTime.now(), pageable)
                        .stream()
                        .map(BookingResponseDto::toDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findFutureBookingsByItemOwnerId(ownerId, LocalDateTime.now(), pageable)
                        .stream()
                        .map(BookingResponseDto::toDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING, pageable)
                        .stream()
                        .map(BookingResponseDto::toDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, pageable)
                        .stream()
                        .map(BookingResponseDto::toDto)
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
    }
}