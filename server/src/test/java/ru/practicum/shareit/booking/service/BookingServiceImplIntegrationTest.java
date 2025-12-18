package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.IllegalArgumentException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.SecurityException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private Long ownerId;
    private Long bookerId;
    private Long itemId;

    @BeforeEach
    void setUp() {
        UserDto ownerDto = UserDto.builder()
                .name("Owner")
                .email("owner@example.com")
                .build();
        UserDto savedOwner = userService.createUser(ownerDto);
        ownerId = savedOwner.getId();

        UserDto bookerDto = UserDto.builder()
                .name("Booker")
                .email("booker@example.com")
                .build();
        UserDto savedBooker = userService.createUser(bookerDto);
        bookerId = savedBooker.getId();

        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();
        ItemDto savedItem = itemService.createItem(itemDto, ownerId);
        itemId = savedItem.getId();
    }

    @Test
    void createBooking_shouldCreateBookingSuccessfully() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(itemId)
                .build();

        BookingResponseDto result = bookingService.createBooking(bookingDto, bookerId);

        assertNotNull(result.getId());
        assertEquals("WAITING", result.getStatus().toString());
        assertEquals(bookerId, result.getBooker().getId());
        assertEquals(itemId, result.getItem().getId());
    }

    @Test
    void createBooking_withUnavailableItem_shouldThrowException() {
        ItemDto updateDto = ItemDto.builder()
                .available(false)
                .build();
        itemService.updateItem(itemId, updateDto, ownerId);

        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(itemId)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(bookingDto, bookerId));
    }

    @Test
    void createBooking_byOwner_shouldThrowException() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(itemId)
                .build();

        assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(bookingDto, ownerId));
    }

    @Test
    void createBooking_withEndBeforeStart_shouldThrowException() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .itemId(itemId)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(bookingDto, bookerId));
    }

    @Test
    void updateBookingStatus_shouldApproveBooking() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(itemId)
                .build();
        BookingResponseDto created = bookingService.createBooking(bookingDto, bookerId);

        BookingResponseDto result = bookingService.updateBookingStatus(
                created.getId(), true, ownerId);

        assertEquals("APPROVED", result.getStatus().toString());
    }

    @Test
    void updateBookingStatus_shouldRejectBooking() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(itemId)
                .build();
        BookingResponseDto created = bookingService.createBooking(bookingDto, bookerId);

        BookingResponseDto result = bookingService.updateBookingStatus(
                created.getId(), false, ownerId);

        assertEquals("REJECTED", result.getStatus().toString());
    }

    @Test
    void updateBookingStatus_byNonOwner_shouldThrowException() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(itemId)
                .build();
        BookingResponseDto created = bookingService.createBooking(bookingDto, bookerId);

        UserDto anotherUser = UserDto.builder()
                .name("Another")
                .email("another@example.com")
                .build();
        UserDto savedAnother = userService.createUser(anotherUser);

        assertThrows(SecurityException.class,
                () -> bookingService.updateBookingStatus(
                        created.getId(), true, savedAnother.getId()));
    }

    @Test
    void getBookingById_shouldReturnBooking() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(itemId)
                .build();
        BookingResponseDto created = bookingService.createBooking(bookingDto, bookerId);

        BookingResponseDto resultByBooker = bookingService.getBookingById(
                created.getId(), bookerId);
        assertEquals(created.getId(), resultByBooker.getId());

        BookingResponseDto resultByOwner = bookingService.getBookingById(
                created.getId(), ownerId);
        assertEquals(created.getId(), resultByOwner.getId());
    }

    @Test
    void getBookingById_byUnauthorizedUser_shouldThrowException() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(itemId)
                .build();
        BookingResponseDto created = bookingService.createBooking(bookingDto, bookerId);

        UserDto stranger = UserDto.builder()
                .name("Stranger")
                .email("stranger@example.com")
                .build();
        UserDto savedStranger = userService.createUser(stranger);

        assertThrows(SecurityException.class,
                () -> bookingService.getBookingById(created.getId(), savedStranger.getId()));
    }

    @Test
    void getUserBookings_shouldReturnBookings() {
        for (int i = 1; i <= 3; i++) {
            BookingDto bookingDto = BookingDto.builder()
                    .start(LocalDateTime.now().plusDays(i))
                    .end(LocalDateTime.now().plusDays(i + 1))
                    .itemId(itemId)
                    .build();
            bookingService.createBooking(bookingDto, bookerId);
        }

        List<BookingResponseDto> result = bookingService.getUserBookings(
                bookerId, "ALL", 0, 10);

        assertEquals(3, result.size());
    }
}