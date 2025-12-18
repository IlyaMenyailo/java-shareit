package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private Long ownerId;
    private Long itemId;
    private Long userId;

    @BeforeEach
    void setUp() {
        UserDto ownerDto = UserDto.builder()
                .name("Owner")
                .email("owner@example.com")
                .build();
        UserDto savedOwner = userService.createUser(ownerDto);
        ownerId = savedOwner.getId();

        UserDto userDto = UserDto.builder()
                .name("User")
                .email("user@example.com")
                .build();
        UserDto savedUser = userService.createUser(userDto);
        userId = savedUser.getId();

        ItemDto itemDto = ItemDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .build();
        ItemDto savedItem = itemService.createItem(itemDto, ownerId);
        itemId = savedItem.getId();
    }

    @Test
    void createItem_shouldCreateItemSuccessfully() {
        ItemDto itemDto = ItemDto.builder()
                .name("New Item")
                .description("New Description")
                .available(true)
                .build();

        ItemDto result = itemService.createItem(itemDto, ownerId);

        assertNotNull(result.getId());
        assertEquals("New Item", result.getName());
        assertEquals("New Description", result.getDescription());
        assertTrue(result.getAvailable());
        assertEquals(ownerId, result.getOwnerId());
    }

    @Test
    void createItem_withNonExistentUser_shouldThrowException() {
        ItemDto itemDto = ItemDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        assertThrows(NotFoundException.class,
                () -> itemService.createItem(itemDto, 999L));
    }

    @Test
    void updateItem_shouldUpdateItemSuccessfully() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .available(false)
                .build();

        ItemDto result = itemService.updateItem(itemId, updateDto, ownerId);

        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void updateItem_byNonOwner_shouldThrowException() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated")
                .build();

        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(itemId, updateDto, userId));
    }

    @Test
    void getItemById_shouldReturnItemWithBookings() {
        ItemWithBookingsDto result = itemService.getItemById(itemId, ownerId);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("Item", result.getName());
        assertEquals("Description", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void getItemsByOwner_shouldReturnOwnerItemsWithPagination() {
        List<ItemWithBookingsDto> result = itemService.getItemsByOwner(ownerId, 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(itemId, result.get(0).getId());
    }

    @Test
    void searchItems_shouldReturnMatchingItemsWithPagination() {
        List<ItemDto> result = itemService.searchItems("item", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Item", result.get(0).getName());
    }

    @Test
    void searchItems_withEmptyText_shouldReturnEmptyList() {
        List<ItemDto> result = itemService.searchItems("", 0, 10);

        assertTrue(result.isEmpty());
    }
}