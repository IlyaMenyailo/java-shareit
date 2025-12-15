package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    @Test
    void toItemDto_shouldConvertItemToDto() {
        Item item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Power drill")
                .available(true)
                .owner(10L)
                .request(20L)
                .build();

        ItemDto dto = ItemMapper.toItemDto(item);

        assertNotNull(dto);
        assertEquals(item.getId(), dto.getId());
        assertEquals(item.getName(), dto.getName());
        assertEquals(item.getDescription(), dto.getDescription());
        assertEquals(item.getAvailable(), dto.getAvailable());
        assertEquals(item.getOwner(), dto.getOwnerId());
        assertEquals(item.getRequest(), dto.getRequestId());
    }

    @Test
    void toItem_shouldConvertDtoToItem() {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Power drill")
                .available(true)
                .requestId(20L)
                .ownerId(10L)
                .build();

        Item item = ItemMapper.toItem(dto);

        assertNotNull(item);
        assertEquals(dto.getId(), item.getId());
        assertEquals(dto.getName(), item.getName());
        assertEquals(dto.getDescription(), item.getDescription());
        assertEquals(dto.getAvailable(), item.getAvailable());
        assertEquals(dto.getRequestId(), item.getRequest());
        assertNull(item.getOwner());
    }

    @Test
    void toItemWithBookingsDto_shouldConvertItemToDto() {
        Item item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Power drill")
                .available(true)
                .request(20L)
                .build();

        ItemWithBookingsDto dto = ItemMapper.toItemWithBookingsDto(item);

        assertNotNull(dto);
        assertEquals(item.getId(), dto.getId());
        assertEquals(item.getName(), dto.getName());
        assertEquals(item.getDescription(), dto.getDescription());
        assertEquals(item.getAvailable(), dto.getAvailable());
        assertEquals(item.getRequest(), dto.getRequest());
        assertNull(dto.getLastBooking());
        assertNull(dto.getNextBooking());
        assertNull(dto.getComments());
    }

    @Test
    void updateItemFromDto_shouldUpdateOnlyProvidedFields() {
        Item item = Item.builder()
                .id(1L)
                .name("Original")
                .description("Original description")
                .available(true)
                .request(10L)
                .build();

        ItemDto updateDto = ItemDto.builder()
                .name("Updated")
                .available(false)
                .build();

        ItemMapper.updateItemFromDto(updateDto, item);

        assertEquals("Updated", item.getName());
        assertEquals("Original description", item.getDescription()); // Не изменилось
        assertFalse(item.getAvailable());
        assertEquals(10L, item.getRequest()); // Не изменилось
    }

    @Test
    void updateItemFromDto_withNullFields_shouldNotUpdate() {
        Item item = Item.builder()
                .id(1L)
                .name("Original")
                .description("Original description")
                .available(true)
                .request(10L)
                .build();

        ItemDto updateDto = new ItemDto(); // Все поля null

        ItemMapper.updateItemFromDto(updateDto, item);

        assertEquals("Original", item.getName());
        assertEquals("Original description", item.getDescription());
        assertTrue(item.getAvailable());
        assertEquals(10L, item.getRequest());
    }

    @Test
    void toCommentDto_shouldConvertCommentToDto() {
        User author = User.builder()
                .id(1L)
                .name("User1")
                .email("user1@example.com")
                .build();

        Item item = Item.builder()
                .id(10L)
                .name("Drill")
                .build();

        LocalDateTime created = LocalDateTime.now();
        Comment comment = Comment.builder()
                .id(1L)
                .text("Great item!")
                .item(item)
                .author(author)
                .created(created)
                .build();

        var dto = ItemMapper.toCommentDto(comment);

        assertNotNull(dto);
        assertEquals(comment.getId(), dto.getId());
        assertEquals(comment.getText(), dto.getText());
        assertEquals(author.getName(), dto.getAuthorName());
        assertEquals(comment.getCreated(), dto.getCreated());
    }

    @Test
    void toItem_withoutOptionalFields_shouldWork() {
        ItemDto dto = ItemDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        Item item = ItemMapper.toItem(dto);

        assertNotNull(item);
        assertEquals("Item", item.getName());
        assertEquals("Description", item.getDescription());
        assertTrue(item.getAvailable());
        assertNull(item.getRequest());
    }
}