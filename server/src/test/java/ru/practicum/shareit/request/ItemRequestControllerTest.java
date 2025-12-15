package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.util.HttpHeaders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void createItemRequest_shouldReturnCreatedRequest() throws Exception {
        ItemRequestShortDto requestDto = ItemRequestShortDto.builder()
                .description("Need a drill")
                .build();

        ItemRequestDto savedRequest = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.createItemRequest(eq(1L), any(ItemRequestShortDto.class)))
                .thenReturn(savedRequest);

        mockMvc.perform(post("/requests")
                        .header(HttpHeaders.USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a drill"));

        verify(itemRequestService).createItemRequest(eq(1L), any(ItemRequestShortDto.class));
    }

    @Test
    void getUserItemRequests_shouldReturnRequests() throws Exception {
        ItemRequestDto request1 = ItemRequestDto.builder().id(1L).description("Request1").build();
        ItemRequestDto request2 = ItemRequestDto.builder().id(2L).description("Request2").build();

        when(itemRequestService.getUserItemRequests(1L))
                .thenReturn(List.of(request1, request2));

        mockMvc.perform(get("/requests")
                        .header(HttpHeaders.USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(itemRequestService).getUserItemRequests(1L);
    }

    @Test
    void getAllItemRequests_shouldReturnAllRequests() throws Exception {
        ItemRequestDto request1 = ItemRequestDto.builder().id(1L).description("Request1").build();

        when(itemRequestService.getAllItemRequests(eq(1L), anyInt(), anyInt()))
                .thenReturn(List.of(request1));

        mockMvc.perform(get("/requests/all")
                        .header(HttpHeaders.USER_ID_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(itemRequestService).getAllItemRequests(1L, 0, 10);
    }

    @Test
    void getItemRequestById_shouldReturnRequest() throws Exception {
        ItemRequestDto request = ItemRequestDto.builder()
                .id(1L)
                .description("Need item")
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.getItemRequestById(1L, 1L))
                .thenReturn(request);

        mockMvc.perform(get("/requests/1")
                        .header(HttpHeaders.USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need item"));

        verify(itemRequestService).getItemRequestById(1L, 1L);
    }

    @Test
    void createItemRequest_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        ItemRequestShortDto requestDto = ItemRequestShortDto.builder()
                .description("Need item")
                .build();

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).createItemRequest(any(), any());
    }

    @Test
    void createItemRequest_withEmptyDescription_shouldReturnBadRequest() throws Exception {
        ItemRequestShortDto invalidRequest = ItemRequestShortDto.builder()
                .description("")  // empty description
                .build();

        mockMvc.perform(post("/requests")
                        .header(HttpHeaders.USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).createItemRequest(any(), any());
    }
}