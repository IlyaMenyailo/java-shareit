package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.util.HttpHeaders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ErrorHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Test
    void handleNotFoundException_shouldReturn404() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Item not found"));

        mockMvc.perform(get("/items/999")
                        .header(HttpHeaders.USER_ID_HEADER, 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Item not found"));
    }

    @Test
    void handleDuplicatedDataException_shouldReturn409() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .name("Test")
                .description("Test")
                .available(true)
                .build();

        when(itemService.createItem(any(ItemDto.class), anyLong()))
                .thenThrow(new DuplicatedDataException("Duplicate item"));

        mockMvc.perform(post("/items")
                        .header(HttpHeaders.USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"description\":\"Test\",\"available\":true}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Duplicate item"));
    }

    @Test
    void handleIllegalArgumentException_shouldReturn400() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any()))
                .thenThrow(new IllegalArgumentException("Invalid comment"));

        mockMvc.perform(post("/items/1/comment")
                        .header(HttpHeaders.USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid comment"));
    }

    @Test
    void handleSecurityException_shouldReturn403() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .name("Updated")
                .build();

        when(itemService.updateItem(anyLong(), any(ItemDto.class), anyLong()))
                .thenThrow(new SecurityException("Access denied"));

        mockMvc.perform(patch("/items/1")
                        .header(HttpHeaders.USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied"));
    }
}