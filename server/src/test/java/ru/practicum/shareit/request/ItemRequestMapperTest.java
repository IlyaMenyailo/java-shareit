package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestMapperTest {

    @Test
    void toItemRequest_shouldConvertShortDtoToItemRequest() {
        User requestor = User.builder()
                .id(1L)
                .name("user1")
                .email("user1@example.com")
                .build();

        ItemRequestShortDto shortDto = ItemRequestShortDto.builder()
                .description("Need a drill")
                .build();

        ItemRequest request = ItemRequestMapper.toItemRequest(shortDto, requestor);

        assertNotNull(request);
        assertNull(request.getId());
        assertEquals("Need a drill", request.getDescription());
        assertEquals(requestor, request.getRequestor());
        assertNotNull(request.getCreated());
        assertTrue(request.getCreated().isBefore(LocalDateTime.now().plusSeconds(1)) ||
                request.getCreated().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void toItemRequestDto_withItems_shouldConvertWithItems() {
        User requestor = User.builder()
                .id(1L)
                .name("user1")
                .build();

        LocalDateTime created = LocalDateTime.now();
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Need tools")
                .requestor(requestor)
                .created(created)
                .build();

        Item item1 = Item.builder()
                .id(10L)
                .name("Drill")
                .description("Power drill")
                .available(true)
                .owner(2L)
                .request(1L)
                .build();

        Item item2 = Item.builder()
                .id(11L)
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .owner(3L)
                .request(1L)
                .build();

        List<Item> items = Arrays.asList(item1, item2);

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request, items);

        assertNotNull(dto);
        assertEquals(request.getId(), dto.getId());
        assertEquals(request.getDescription(), dto.getDescription());
        assertEquals(request.getCreated(), dto.getCreated());
        assertEquals(2, dto.getItems().size());

        ItemDto itemDto1 = dto.getItems().get(0);
        assertEquals(item1.getId(), itemDto1.getId());
        assertEquals(item1.getName(), itemDto1.getName());
        assertEquals(item1.getAvailable(), itemDto1.getAvailable());

        ItemDto itemDto2 = dto.getItems().get(1);
        assertEquals(item2.getId(), itemDto2.getId());
        assertEquals(item2.getName(), itemDto2.getName());
    }

    @Test
    void toItemRequestDto_withEmptyItems_shouldConvertWithEmptyList() {
        User requestor = User.builder()
                .id(1L)
                .name("user1")
                .build();

        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Need tools")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request, Collections.emptyList());

        assertNotNull(dto);
        assertEquals(request.getId(), dto.getId());
        assertEquals(request.getDescription(), dto.getDescription());
        assertNotNull(dto.getItems());
        assertTrue(dto.getItems().isEmpty());
    }

    @Test
    void toItemRequestDto_withoutItems_shouldConvertWithoutItems() {
        User requestor = User.builder()
                .id(1L)
                .name("user1")
                .build();

        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Need tools")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);

        assertNotNull(dto);
        assertEquals(request.getId(), dto.getId());
        assertEquals(request.getDescription(), dto.getDescription());
        assertNotNull(dto.getCreated());
        assertNull(dto.getItems());
    }

    @Test
    void toItemRequest_withNullDto_shouldThrowException() {
        User requestor = User.builder().id(1L).build();

        assertThrows(NullPointerException.class,
                () -> ItemRequestMapper.toItemRequest(null, requestor));
    }
}