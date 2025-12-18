package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.strategy.BookingStrategyFactory;
import ru.practicum.shareit.booking.strategy.BookingStateStrategy;
import ru.practicum.shareit.exception.IllegalArgumentException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.SecurityException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingStrategyFactory bookingStrategyFactory;

    @Mock
    private BookingStateStrategy bookingStateStrategy;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingDto bookingDto;
    private User booker;
    private Item item;
    private Booking booking;
    private Long bookingId;
    private Long bookerId;
    private Long ownerId;

    @BeforeEach
    void setUp() {
        bookerId = 1L;
        ownerId = 2L;
        bookingId = 1L;
        Long itemId = 1L;

        booker = User.builder()
                .id(bookerId)
                .name("Booker")
                .email("booker@example.com")
                .build();

        item = Item.builder()
                .id(itemId)
                .name("Item")
                .description("Description")
                .available(true)
                .owner(ownerId)
                .build();

        bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(itemId)
                .status(BookingStatus.WAITING)
                .build();

        booking = Booking.builder()
                .id(bookingId)
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void createBooking_shouldCreateBookingSuccessfully() {
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(bookingDto.getItemId())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.createBooking(bookingDto, bookerId);

        assertNotNull(result);
        assertEquals(BookingStatus.WAITING, result.getStatus());
        assertEquals(bookerId, result.getBooker().getId());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void createBooking_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(bookerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDto, bookerId));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_whenItemNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(bookingDto.getItemId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDto, bookerId));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_whenItemNotAvailable_shouldThrowIllegalArgumentException() {
        item.setAvailable(false);

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(bookingDto.getItemId())).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(bookingDto, bookerId));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_whenOwnerBooksOwnItem_shouldThrowNotFoundException() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(bookingDto.getItemId())).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDto, ownerId));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_whenEndBeforeStart_shouldThrowIllegalArgumentException() {
        bookingDto.setStart(LocalDateTime.now().plusDays(2));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(bookingDto.getItemId())).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(bookingDto, bookerId));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBookingStatus_shouldApproveBookingSuccessfully() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.updateBookingStatus(bookingId, true, ownerId);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, result.getStatus());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void updateBookingStatus_shouldRejectBookingSuccessfully() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.updateBookingStatus(bookingId, false, ownerId);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, result.getStatus());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void updateBookingStatus_whenBookingNotFound_shouldThrowNotFoundException() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.updateBookingStatus(bookingId, true, ownerId));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBookingStatus_whenNotOwner_shouldThrowSecurityException() {
        Long notOwnerId = 3L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(SecurityException.class,
                () -> bookingService.updateBookingStatus(bookingId, true, notOwnerId));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBookingStatus_whenAlreadyProcessed_shouldThrowIllegalArgumentException() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.updateBookingStatus(bookingId, true, ownerId));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getBookingById_shouldReturnBookingForBooker() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getBookingById(bookingId, bookerId);

        assertNotNull(result);
        assertEquals(bookingId, result.getId());
    }

    @Test
    void getBookingById_shouldReturnBookingForOwner() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getBookingById(bookingId, ownerId);

        assertNotNull(result);
        assertEquals(bookingId, result.getId());
    }

    @Test
    void getBookingById_whenBookingNotFound_shouldThrowNotFoundException() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(bookingId, bookerId));
    }

    @Test
    void getBookingById_whenUnauthorizedUser_shouldThrowSecurityException() {
        Long unauthorizedUserId = 3L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(SecurityException.class,
                () -> bookingService.getBookingById(bookingId, unauthorizedUserId));
    }

    @Test
    void getUserBookings_shouldReturnBookings() {
        Integer from = 0;
        Integer size = 10;
        String state = "ALL";

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingStrategyFactory.getUserStrategy(BookingState.ALL)).thenReturn(bookingStateStrategy);
        when(bookingStateStrategy.findBookings(eq(bookerId), any(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(bookerId, state, from, size);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(bookingId, result.get(0).getId());
    }

    @Test
    void getUserBookings_whenUserNotFound_shouldThrowNotFoundException() {
        Integer from = 0;
        Integer size = 10;
        String state = "ALL";

        when(userRepository.findById(bookerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.getUserBookings(bookerId, state, from, size));
    }

    @Test
    void getOwnerBookings_shouldReturnOwnerBookings() {
        Integer from = 0;
        Integer size = 10;
        String state = "ALL";

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(booker));
        when(bookingStrategyFactory.getOwnerStrategy(BookingState.ALL)).thenReturn(bookingStateStrategy);
        when(bookingStateStrategy.findBookings(eq(ownerId), any(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(ownerId, state, from, size);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(bookingId, result.get(0).getId());
    }

    @Test
    void getOwnerBookings_whenOwnerNotFound_shouldThrowNotFoundException() {
        Integer from = 0;
        Integer size = 10;
        String state = "ALL";

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.getOwnerBookings(ownerId, state, from, size));
    }
}