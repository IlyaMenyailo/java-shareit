package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.util.HttpHeaders;

import java.time.LocalDateTime;

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
    private BookingClient bookingClient;

    private BookingDto bookingDto;
    private Long userId = 1L;
    private Long bookingId = 1L;

    @BeforeEach
    void setUp() {
        bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(10L)
                .build();
    }

    @Test
    void createBooking_shouldReturnCreatedBooking() throws Exception {
        when(bookingClient.createBooking(any(BookingDto.class), eq(userId)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.CREATED));

        mockMvc.perform(post("/bookings")
                        .header(HttpHeaders.USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isCreated());

        verify(bookingClient).createBooking(any(BookingDto.class), eq(userId));
    }

    @Test
    void updateBookingStatus_shouldReturnUpdatedBooking() throws Exception {
        when(bookingClient.updateBookingStatus(eq(bookingId), eq(true), eq(userId)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(HttpHeaders.USER_ID_HEADER, userId)
                        .param("approved", "true"))
                .andExpect(status().isOk());

        verify(bookingClient).updateBookingStatus(bookingId, true, userId);
    }

    @Test
    void getBookingById_shouldReturnBooking() throws Exception {
        when(bookingClient.getBookingById(bookingId, userId))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(HttpHeaders.USER_ID_HEADER, userId))
                .andExpect(status().isOk());

        verify(bookingClient).getBookingById(bookingId, userId);
    }

    @Test
    void getUserBookings_shouldReturnBookings() throws Exception {
        when(bookingClient.getUserBookings(eq(userId), anyString(), anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/bookings")
                        .header(HttpHeaders.USER_ID_HEADER, userId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(bookingClient).getUserBookings(userId, "ALL", 0, 10);
    }

    @Test
    void getUserBookings_withDefaultParams_shouldUseDefaults() throws Exception {
        when(bookingClient.getUserBookings(eq(userId), eq("ALL"), eq(0), eq(10)))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/bookings")
                        .header(HttpHeaders.USER_ID_HEADER, userId))
                .andExpect(status().isOk());

        verify(bookingClient).getUserBookings(userId, "ALL", 0, 10);
    }

    @Test
    void getOwnerBookings_shouldReturnOwnerBookings() throws Exception {
        when(bookingClient.getOwnerBookings(eq(userId), anyString(), anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/bookings/owner")
                        .header(HttpHeaders.USER_ID_HEADER, userId)
                        .param("state", "CURRENT")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(bookingClient).getOwnerBookings(userId, "CURRENT", 0, 5);
    }

    @Test
    void createBooking_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).createBooking(any(), any());
    }
}