package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private Long userId;
    private Long anotherUserId;

    @BeforeEach
    void setUp() {
        UserDto userDto = UserDto.builder()
                .name("User")
                .email("user@example.com")
                .build();
        UserDto savedUser = userService.createUser(userDto);
        userId = savedUser.getId();

        UserDto anotherUserDto = UserDto.builder()
                .name("Another User")
                .email("another@example.com")
                .build();
        UserDto savedAnotherUser = userService.createUser(anotherUserDto);
        anotherUserId = savedAnotherUser.getId();
    }

    @Test
    void createItemRequest_withNonExistentUser_shouldThrowException() {
        ItemRequestShortDto requestDto = ItemRequestShortDto.builder()
                .description("Need item")
                .build();

        assertThrows(NotFoundException.class,
                () -> itemRequestService.createItemRequest(999L, requestDto));
    }

    @Test
    void getUserItemRequests_shouldReturnUserRequests() {
        for (int i = 1; i <= 3; i++) {
            ItemRequestShortDto requestDto = ItemRequestShortDto.builder()
                    .description("Request " + i)
                    .build();
            itemRequestService.createItemRequest(userId, requestDto);
        }

        List<ItemRequestDto> result = itemRequestService.getUserItemRequests(userId);

        assertEquals(3, result.size());
        assertEquals("Request 3", result.get(0).getDescription());
    }

    @Test
    void getAllItemRequests_shouldReturnOtherUsersRequests() {
        ItemRequestShortDto request1 = ItemRequestShortDto.builder()
                .description("User 1 request")
                .build();
        itemRequestService.createItemRequest(userId, request1);

        ItemRequestShortDto request2 = ItemRequestShortDto.builder()
                .description("User 2 request")
                .build();
        itemRequestService.createItemRequest(anotherUserId, request2);

        List<ItemRequestDto> result = itemRequestService.getAllItemRequests(userId, 0, 10);

        assertEquals(1, result.size());
        assertEquals("User 2 request", result.get(0).getDescription());
    }

    @Test
    void getAllItemRequests_withPagination_shouldReturnPaginatedResults() {
        for (int i = 1; i <= 15; i++) {
            ItemRequestShortDto requestDto = ItemRequestShortDto.builder()
                    .description("Request " + i)
                    .build();
            itemRequestService.createItemRequest(anotherUserId, requestDto);
        }

        List<ItemRequestDto> page1 = itemRequestService.getAllItemRequests(userId, 0, 5);
        assertEquals(5, page1.size());

        List<ItemRequestDto> page2 = itemRequestService.getAllItemRequests(userId, 5, 5);
        assertEquals(5, page2.size());

        assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
    }

    @Test
    void getItemRequestById_shouldReturnRequestWithItems() {
        ItemRequestShortDto requestDto = ItemRequestShortDto.builder()
                .description("Need a drill")
                .build();
        ItemRequestDto created = itemRequestService.createItemRequest(userId, requestDto);

        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .requestId(created.getId())
                .build();
        itemService.createItem(itemDto, anotherUserId);

        ItemRequestDto result = itemRequestService.getItemRequestById(userId, created.getId());

        assertEquals(created.getId(), result.getId());
        assertEquals("Need a drill", result.getDescription());
        assertEquals(1, result.getItems().size());
        assertEquals("Drill", result.getItems().get(0).getName());
    }

    @Test
    void getItemRequestById_nonExistentRequest_shouldThrowException() {
        assertThrows(NotFoundException.class,
                () -> itemRequestService.getItemRequestById(userId, 999L));
    }
}