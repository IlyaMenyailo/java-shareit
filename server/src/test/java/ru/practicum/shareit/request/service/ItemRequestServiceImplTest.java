package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
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
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User requestor;
    private ItemRequestShortDto requestShortDto;
    private ItemRequest itemRequest;
    private Long userId;
    private Long requestId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        requestId = 1L;

        requestor = User.builder()
                .id(userId)
                .name("Requestor")
                .email("requestor@example.com")
                .build();

        requestShortDto = ItemRequestShortDto.builder()
                .description("Need item")
                .build();

        itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("Need item")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void createItemRequest_shouldCreateRequestSuccessfully() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestDto result = itemRequestService.createItemRequest(userId, requestShortDto);

        assertNotNull(result);
        assertEquals(requestShortDto.getDescription(), result.getDescription());
        assertNotNull(result.getCreated());
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void createItemRequest_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.createItemRequest(userId, requestShortDto));
        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    void getUserItemRequests_shouldReturnRequestsWithItems() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId))
                .thenReturn(List.of(itemRequest));

        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Item description")
                .available(true)
                .owner(2L)
                .request(requestId)
                .build();

        when(itemRepository.findByRequestIn(List.of(requestId))).thenReturn(List.of(item));

        List<ItemRequestDto> result = itemRequestService.getUserItemRequests(userId);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertFalse(result.get(0).getItems().isEmpty());
        assertEquals("Item", result.get(0).getItems().get(0).getName());
    }

    @Test
    void getUserItemRequests_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getUserItemRequests(userId));
        verify(itemRequestRepository, never()).findByRequestorIdOrderByCreatedDesc(anyLong());
    }

    @Test
    void getUserItemRequests_withEmptyRequests_shouldReturnEmptyList() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = itemRequestService.getUserItemRequests(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllItemRequests_shouldReturnOtherUsersRequests() {
        Integer from = 0;
        Integer size = 10;

        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(eq(userId), any(Pageable.class)))
                .thenReturn(List.of(itemRequest));

        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Item description")
                .available(true)
                .owner(2L)
                .request(requestId)
                .build();

        when(itemRepository.findByRequestIn(List.of(requestId))).thenReturn(List.of(item));

        List<ItemRequestDto> result = itemRequestService.getAllItemRequests(userId, from, size);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertFalse(result.get(0).getItems().isEmpty());
    }

    @Test
    void getAllItemRequests_whenUserNotFound_shouldThrowNotFoundException() {
        Integer from = 0;
        Integer size = 10;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllItemRequests(userId, from, size));
    }

    @Test
    void getAllItemRequests_withEmptyResults_shouldReturnEmptyList() {
        Integer from = 0;
        Integer size = 10;

        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(eq(userId), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = itemRequestService.getAllItemRequests(userId, from, size);

        assertTrue(result.isEmpty());
    }

    @Test
    void getItemRequestById_shouldReturnRequestWithItems() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));

        // Mock items
        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Item description")
                .available(true)
                .owner(2L)
                .request(requestId)
                .build();

        when(itemRepository.findByRequest(requestId)).thenReturn(List.of(item));

        ItemRequestDto result = itemRequestService.getItemRequestById(userId, requestId);

        assertNotNull(result);
        assertEquals(requestShortDto.getDescription(), result.getDescription());
        assertFalse(result.getItems().isEmpty());
        assertEquals("Item", result.getItems().get(0).getName());
    }

    @Test
    void getItemRequestById_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.getItemRequestById(userId, requestId));
    }

    @Test
    void getItemRequestById_whenRequestNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.getItemRequestById(userId, requestId));
    }
}