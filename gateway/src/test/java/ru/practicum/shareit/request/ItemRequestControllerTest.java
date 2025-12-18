package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.util.HttpHeaders;

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
    private ItemRequestClient itemRequestClient;

    private ItemRequestShortDto itemRequestShortDto;
    private Long userId = 1L;
    private Long requestId = 1L;

    @BeforeEach
    void setUp() {
        itemRequestShortDto = ItemRequestShortDto.builder()
                .description("Need a drill for home repairs")
                .build();
    }

    @Test
    void createItemRequest_withEmptyDescription_shouldReturnBadRequest() throws Exception {
        ItemRequestShortDto invalidDto = ItemRequestShortDto.builder()
                .description("")
                .build();

        mockMvc.perform(post("/requests")
                        .header(HttpHeaders.USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).createItemRequest(any(), any());
    }

    @Test
    void getUserItemRequests_shouldReturnRequests() throws Exception {
        when(itemRequestClient.getUserItemRequests(userId))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/requests")
                        .header(HttpHeaders.USER_ID_HEADER, userId))
                .andExpect(status().isOk());

        verify(itemRequestClient).getUserItemRequests(userId);
    }

    @Test
    void getAllItemRequests_shouldReturnAllRequests() throws Exception {
        when(itemRequestClient.getAllItemRequests(eq(userId), anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/requests/all")
                        .header(HttpHeaders.USER_ID_HEADER, userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(itemRequestClient).getAllItemRequests(userId, 0, 10);
    }

    @Test
    void getAllItemRequests_withInvalidPagination_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header(HttpHeaders.USER_ID_HEADER, userId)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).getAllItemRequests(any(), anyInt(), anyInt());
    }

    @Test
    void getItemRequestById_shouldReturnRequest() throws Exception {
        when(itemRequestClient.getItemRequestById(requestId, userId))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(HttpHeaders.USER_ID_HEADER, userId))
                .andExpect(status().isOk());

        verify(itemRequestClient).getItemRequestById(requestId, userId);
    }

    @Test
    void createItemRequest_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestShortDto)))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).createItemRequest(any(), any());
    }
}