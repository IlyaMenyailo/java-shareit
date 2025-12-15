package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.util.HttpHeaders;


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
    private ItemClient itemClient;

    private ItemDto itemDto;
    private CreateCommentDto createCommentDto;
    private Long ownerId;
    private Long userId;
    private Long itemId;

    @BeforeEach
    void setUp() {
        ownerId = 1L;
        userId = 2L;
        itemId = 10L;

        itemDto = ItemDto.builder()
                .id(itemId)
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        createCommentDto = CreateCommentDto.builder()
                .text("Отличная дрель!")
                .build();
    }

    @Test
    void createItem_shouldCallClientAndReturnCreated() throws Exception {
        when(itemClient.createItem(any(ItemDto.class), eq(ownerId)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.CREATED));

        mockMvc.perform(post("/items")
                        .header(HttpHeaders.USER_ID_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isCreated());

        verify(itemClient).createItem(any(ItemDto.class), eq(ownerId));
    }

    @Test
    void createItem_withInvalidData_shouldReturnBadRequest() throws Exception {
        ItemDto invalidItemDto = ItemDto.builder()
                .name("")
                .description("")
                .build();

        mockMvc.perform(post("/items")
                        .header(HttpHeaders.USER_ID_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItemDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).createItem(any(), any());
    }

    @Test
    void createItem_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).createItem(any(), any());
    }

    @Test
    void updateItem_shouldCallClientAndReturnOk() throws Exception {
        when(itemClient.updateItem(eq(itemId), any(ItemDto.class), eq(ownerId)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(HttpHeaders.USER_ID_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isOk());

        verify(itemClient).updateItem(eq(itemId), any(ItemDto.class), eq(ownerId));
    }

    @Test
    void getItemById_shouldCallClientAndReturnOk() throws Exception {
        when(itemClient.getItemById(itemId, userId))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(HttpHeaders.USER_ID_HEADER, userId))
                .andExpect(status().isOk());

        verify(itemClient).getItemById(itemId, userId);
    }

    @Test
    void getItemById_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).getItemById(any(), any());
    }

    @Test
    void getItemsByOwner_shouldCallClientAndReturnOk() throws Exception {
        when(itemClient.getItemsByOwner(ownerId))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/items")
                        .header(HttpHeaders.USER_ID_HEADER, ownerId))
                .andExpect(status().isOk());

        verify(itemClient).getItemsByOwner(ownerId);
    }

    @Test
    void getItemsByOwner_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).getItemsByOwner(any());
    }

    @Test
    void searchItems_shouldCallClientAndReturnOk() throws Exception {
        when(itemClient.searchItems("дрель"))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk());

        verify(itemClient).searchItems("дрель");
    }

    @Test
    void searchItems_withEmptyText_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(itemClient, never()).searchItems(any());
    }

    @Test
    void searchItems_withInvalidPagination_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "test")
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).searchItems(any());
    }

    @Test
    void addComment_shouldCallClientAndReturnOk() throws Exception {
        when(itemClient.addComment(eq(itemId), eq(userId), any(CreateCommentDto.class)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(HttpHeaders.USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCommentDto)))
                .andExpect(status().isOk());

        verify(itemClient).addComment(eq(itemId), eq(userId), any(CreateCommentDto.class));
    }

    @Test
    void addComment_withInvalidData_shouldReturnBadRequest() throws Exception {
        CreateCommentDto invalidComment = CreateCommentDto.builder()
                .text("")
                .build();

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(HttpHeaders.USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidComment)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).addComment(any(), any(), any());
    }

    @Test
    void addComment_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCommentDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).addComment(any(), any(), any());
    }

    @Test
    void handleClientError_shouldPropagateStatusCode() throws Exception {
        when(itemClient.getItemById(itemId, userId))
                .thenReturn(new ResponseEntity<>("{\"error\":\"Not found\"}", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(HttpHeaders.USER_ID_HEADER, userId))
                .andExpect(status().isNotFound());

        verify(itemClient).getItemById(itemId, userId);
    }
}