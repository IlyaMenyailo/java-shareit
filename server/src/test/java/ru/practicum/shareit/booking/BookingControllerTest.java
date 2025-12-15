package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.util.HttpHeaders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void createBooking_shouldReturnCreatedBooking() throws Exception {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingResponseDto savedBooking = BookingResponseDto.builder()
                .id(1L)
                .build();

        when(bookingService.createBooking(any(BookingDto.class), eq(1L)))
                .thenReturn(savedBooking);

        mockMvc.perform(post("/bookings")
                        .header(HttpHeaders.USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(bookingService).createBooking(any(BookingDto.class), eq(1L));
    }

    @Test
    void updateBookingStatus_shouldReturnUpdatedBooking() throws Exception {
        BookingResponseDto updatedBooking = BookingResponseDto.builder()
                .id(1L)
                .build();

        when(bookingService.updateBookingStatus(eq(1L), eq(true), eq(1L)))
                .thenReturn(updatedBooking);

        mockMvc.perform(patch("/bookings/1")
                        .header(HttpHeaders.USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(bookingService).updateBookingStatus(1L, true, 1L);
    }

    @Test
    void getBookingById_shouldReturnBooking() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(1L)
                .build();

        when(bookingService.getBookingById(1L, 1L))
                .thenReturn(booking);

        mockMvc.perform(get("/bookings/1")
                        .header(HttpHeaders.USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(bookingService).getBookingById(1L, 1L);
    }

    @Test
    void getUserBookings_shouldReturnBookings() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder().id(1L).build();

        when(bookingService.getUserBookings(eq(1L), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings")
                        .header(HttpHeaders.USER_ID_HEADER, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(bookingService).getUserBookings(1L, "ALL", 0, 10);
    }

    @Test
    void getOwnerBookings_shouldReturnOwnerBookings() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder().id(1L).build();

        when(bookingService.getOwnerBookings(eq(1L), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings/owner")
                        .header(HttpHeaders.USER_ID_HEADER, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(bookingService).getOwnerBookings(1L, "ALL", 0, 10);
    }

    @Test
    void createBooking_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).createBooking(any(), any());
    }
}