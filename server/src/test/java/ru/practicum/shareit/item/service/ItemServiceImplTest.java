package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.IllegalArgumentException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private ItemDto itemDto;
    private Item item;
    private User owner;
    private Long itemId;
    private Long ownerId;

    @BeforeEach
    void setUp() {
        ownerId = 1L;
        itemId = 1L;

        owner = User.builder()
                .id(ownerId)
                .name("Owner")
                .email("owner@example.com")
                .build();

        itemDto = ItemDto.builder()
                .name("Item")
                .description("Item description")
                .available(true)
                .build();

        item = Item.builder()
                .id(itemId)
                .name("Item")
                .description("Item description")
                .available(true)
                .owner(ownerId)
                .build();
    }

    @Test
    void createItem_shouldCreateItemSuccessfully() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.createItem(itemDto, ownerId);

        assertNotNull(result);
        assertEquals(itemDto.getName(), result.getName());
        assertEquals(itemDto.getDescription(), result.getDescription());
        assertEquals(ownerId, result.getOwnerId());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void createItem_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(itemDto, ownerId));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void createItem_withRequestId_shouldValidateRequestExists() {
        Long requestId = 1L;
        itemDto.setRequestId(requestId);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(mock(ItemRequest.class)));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        assertDoesNotThrow(() -> itemService.createItem(itemDto, ownerId));
        verify(itemRequestRepository, times(1)).findById(requestId);
    }

    @Test
    void createItem_withNonExistentRequestId_shouldThrowNotFoundException() {
        Long requestId = 1L;
        itemDto.setRequestId(requestId);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(itemDto, ownerId));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_shouldUpdateItemSuccessfully() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto updateDto = ItemDto.builder()
                .name("Updated Item")
                .description("Updated description")
                .available(false)
                .build();

        ItemDto result = itemService.updateItem(itemId, updateDto, ownerId);

        assertNotNull(result);
        assertEquals("Updated Item", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertFalse(result.getAvailable());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void updateItem_whenItemNotFound_shouldThrowNotFoundException() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.updateItem(itemId, itemDto, ownerId));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_whenNotOwner_shouldThrowNotFoundException() {
        Long differentOwnerId = 2L;
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> itemService.updateItem(itemId, itemDto, differentOwnerId));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void getItemById_shouldReturnItemWithBookingsForOwner() {
        Long userId = ownerId;
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        Booking lastBooking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .booker(owner)
                .build();

        Booking nextBooking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .booker(owner)
                .build();

        when(bookingRepository.findFirstByItemIdAndEndBeforeOrderByEndDesc(
                eq(itemId), any(LocalDateTime.class)))
                .thenReturn(Optional.of(lastBooking));

        when(bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(
                eq(itemId), any(LocalDateTime.class)))
                .thenReturn(Optional.of(nextBooking));

        Comment comment = Comment.builder()
                .id(1L)
                .text("Great item!")
                .author(owner)
                .created(LocalDateTime.now())
                .build();

        when(commentRepository.findByItemId(itemId)).thenReturn(List.of(comment));

        ItemWithBookingsDto result = itemService.getItemById(itemId, userId);

        assertNotNull(result);
        assertEquals(item.getName(), result.getName());
        assertNotNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());
        assertFalse(result.getComments().isEmpty());
        assertEquals("Great item!", result.getComments().get(0).getText());
    }

    @Test
    void getItemById_shouldReturnItemWithoutBookingsForNonOwner() {
        Long nonOwnerId = 2L;
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        Comment comment = Comment.builder()
                .id(1L)
                .text("Great item!")
                .author(owner)
                .created(LocalDateTime.now())
                .build();

        when(commentRepository.findByItemId(itemId)).thenReturn(List.of(comment));

        ItemWithBookingsDto result = itemService.getItemById(itemId, nonOwnerId);

        assertNotNull(result);
        assertEquals(item.getName(), result.getName());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertFalse(result.getComments().isEmpty());
    }

    @Test
    void getItemById_whenItemNotFound_shouldThrowNotFoundException() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItemById(itemId, ownerId));
    }

    @Test
    void searchItems_shouldReturnEmptyListForEmptyText() {
        List<ItemDto> result = itemService.searchItems("   ");

        assertTrue(result.isEmpty());
        verify(itemRepository, never()).search(anyString());
    }

    @Test
    void searchItems_shouldReturnItemsForValidText() {
        String searchText = "item";
        when(itemRepository.search(searchText)).thenReturn(List.of(item));

        List<ItemDto> result = itemService.searchItems(searchText);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(item.getName(), result.get(0).getName());
    }

    @Test
    void addComment_shouldAddCommentSuccessfully() {
        Long userId = 2L;
        User author = User.builder().id(userId).name("Author").build();
        CreateCommentDto commentDto = CreateCommentDto.builder().text("Great item!").build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(author));

        Booking pastBooking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingRepository.findPastBookingsByItemIdAndBookerId(itemId, userId))
                .thenReturn(List.of(pastBooking));

        Comment savedComment = Comment.builder()
                .id(1L)
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentDto result = itemService.addComment(itemId, userId, commentDto);

        assertNotNull(result);
        assertEquals(commentDto.getText(), result.getText());
        assertEquals(author.getName(), result.getAuthorName());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void addComment_whenUserHasNoPastBookings_shouldThrowIllegalArgumentException() {
        Long userId = 2L;
        CreateCommentDto commentDto = CreateCommentDto.builder().text("Great item!").build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
        when(bookingRepository.findPastBookingsByItemIdAndBookerId(itemId, userId))
                .thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class,
                () -> itemService.addComment(itemId, userId, commentDto));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void deleteItem_shouldDeleteItemSuccessfully() {
        doNothing().when(itemRepository).deleteById(itemId);

        assertDoesNotThrow(() -> itemService.deleteItem(itemId));
        verify(itemRepository, times(1)).deleteById(itemId);
    }
}