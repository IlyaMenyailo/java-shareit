package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.util.HttpHeaders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        ItemDto savedItem = ItemDto.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .ownerId(1L)
                .build();

        when(itemService.createItem(any(ItemDto.class), eq(1L)))
                .thenReturn(savedItem);

        mockMvc.perform(post("/items")
                        .header(HttpHeaders.USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Item"));

        verify(itemService).createItem(any(ItemDto.class), eq(1L));
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated")
                .build();

        ItemDto updatedItem = ItemDto.builder()
                .id(1L)
                .name("Updated")
                .description("Description")
                .available(true)
                .build();

        when(itemService.updateItem(eq(1L), any(ItemDto.class), eq(1L)))
                .thenReturn(updatedItem);

        mockMvc.perform(patch("/items/1")
                        .header(HttpHeaders.USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated"));

        verify(itemService).updateItem(eq(1L), any(ItemDto.class), eq(1L));
    }

    @Test
    void getItemById_shouldReturnItem() throws Exception {
        ItemWithBookingsDto itemDto = ItemWithBookingsDto.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        when(itemService.getItemById(1L, 1L))
                .thenReturn(itemDto);

        mockMvc.perform(get("/items/1")
                        .header(HttpHeaders.USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Item"));

        verify(itemService).getItemById(1L, 1L);
    }

    @Test
    void getItemsByOwner_shouldReturnItemList() throws Exception {
        ItemWithBookingsDto item1 = ItemWithBookingsDto.builder().id(1L).name("Item1").build();
        ItemWithBookingsDto item2 = ItemWithBookingsDto.builder().id(2L).name("Item2").build();

        when(itemService.getItemsByOwner(eq(1L), anyInt(), anyInt()))
                .thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items")
                        .header(HttpHeaders.USER_ID_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(itemService).getItemsByOwner(eq(1L), eq(0), eq(10));
    }

    @Test
    void getItemsByOwner_withDefaultPagination_shouldUseDefaults() throws Exception {
        ItemWithBookingsDto item1 = ItemWithBookingsDto.builder().id(1L).name("Item1").build();

        when(itemService.getItemsByOwner(eq(1L), eq(0), eq(10)))
                .thenReturn(List.of(item1));

        mockMvc.perform(get("/items")
                        .header(HttpHeaders.USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemService).getItemsByOwner(eq(1L), eq(0), eq(10));
    }

    @Test
    void searchItems_shouldReturnSearchResults() throws Exception {
        ItemDto item1 = ItemDto.builder().id(1L).name("Drill").build();
        ItemDto item2 = ItemDto.builder().id(2L).name("Hammer").build();

        when(itemService.searchItems(eq("tool"), anyInt(), anyInt()))
                .thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items/search")
                        .param("text", "tool")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(itemService).searchItems(eq("tool"), eq(0), eq(10));
    }

    @Test
    void searchItems_withEmptyText_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(itemService, never()).searchItems(anyString(), anyInt(), anyInt());
    }

    @Test
    void searchItems_withBlankText_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "   ")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(itemService, never()).searchItems(anyString(), anyInt(), anyInt());
    }

    @Test
    void addComment_shouldReturnCreatedComment() throws Exception {
        CreateCommentDto createComment = CreateCommentDto.builder()
                .text("Great item!")
                .build();

        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Great item!")
                .authorName("John")
                .created(LocalDateTime.now())
                .build();

        when(itemService.addComment(eq(1L), eq(1L), any(CreateCommentDto.class)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header(HttpHeaders.USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Great item!"));

        verify(itemService).addComment(eq(1L), eq(1L), any(CreateCommentDto.class));
    }

    @Test
    void createItem_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .name("Item")
                .description("Desc")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).createItem(any(), any());
    }
}