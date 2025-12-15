package ru.practicum.server.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemForBookingDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserForBookingDto;
import ru.practicum.shareit.user.model.User;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingResponseDtoJsonTest {

    @Autowired
    private JacksonTester<BookingResponseDto> json;

    private ObjectMapper objectMapper;
    private LocalDateTime testStartTime;
    private LocalDateTime testEndTime;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testStartTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        testEndTime = LocalDateTime.of(2024, 1, 2, 10, 0, 0);
    }

    @Test
    void testSerialize() throws IOException {
        UserForBookingDto booker = UserForBookingDto.builder().id(1L).build();
        ItemForBookingDto item = ItemForBookingDto.builder().id(100L).name("Drill").build();

        BookingResponseDto dto = BookingResponseDto.builder()
                .id(1L)
                .start(testStartTime)
                .end(testEndTime)
                .status(BookingStatus.APPROVED)
                .booker(booker)
                .item(item)
                .build();

        String jsonString = json.write(dto).getJson();

        assertThat(jsonString).contains("\"id\":1");
        assertThat(jsonString).contains("\"status\":\"APPROVED\"");
        assertThat(jsonString).contains("\"booker\":{\"id\":1}");
        assertThat(jsonString).contains("\"item\":{\"id\":100,\"name\":\"Drill\"}");
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = "{" +
                "\"id\": 1," +
                "\"start\": \"2024-01-01T10:00:00\"," +
                "\"end\": \"2024-01-02T10:00:00\"," +
                "\"status\": \"WAITING\"," +
                "\"booker\": {\"id\": 1}," +
                "\"item\": {\"id\": 100, \"name\": \"Hammer\"}" +
                "}";

        BookingResponseDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStart()).isEqualTo(testStartTime);
        assertThat(result.getEnd()).isEqualTo(testEndTime);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(result.getBooker().getId()).isEqualTo(1L);
        assertThat(result.getItem().getId()).isEqualTo(100L);
        assertThat(result.getItem().getName()).isEqualTo("Hammer");
    }

    @Test
    void testToDtoMethod() {
        User booker = User.builder()
                .id(1L)
                .name("Booker")
                .email("booker@example.com")
                .build();

        Item item = Item.builder()
                .id(100L)
                .name("Power Drill")
                .description("Professional drill")
                .available(true)
                .owner(2L)
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .start(testStartTime)
                .end(testEndTime)
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        BookingResponseDto dto = BookingResponseDto.toDto(booking);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(testStartTime);
        assertThat(dto.getEnd()).isEqualTo(testEndTime);
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(dto.getBooker().getId()).isEqualTo(1L);
        assertThat(dto.getItem().getId()).isEqualTo(100L);
        assertThat(dto.getItem().getName()).isEqualTo("Power Drill");
    }

    @Test
    void testBuilder() {
        UserForBookingDto booker = UserForBookingDto.builder().id(1L).build();
        ItemForBookingDto item = ItemForBookingDto.builder().id(100L).name("Builder Item").build();

        BookingResponseDto dto = BookingResponseDto.builder()
                .id(1L)
                .start(testStartTime)
                .end(testEndTime)
                .status(BookingStatus.WAITING)
                .booker(booker)
                .item(item)
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(testStartTime);
        assertThat(dto.getEnd()).isEqualTo(testEndTime);
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(dto.getBooker()).isEqualTo(booker);
        assertThat(dto.getItem()).isEqualTo(item);
    }

    @Test
    void testNoArgsConstructor() {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(1L);
        dto.setStart(testStartTime);
        dto.setEnd(testEndTime);
        dto.setStatus(BookingStatus.REJECTED);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(testStartTime);
        assertThat(dto.getEnd()).isEqualTo(testEndTime);
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void testAllArgsConstructor() {
        UserForBookingDto booker = UserForBookingDto.builder().id(1L).build();
        ItemForBookingDto item = ItemForBookingDto.builder().id(100L).name("Item").build();

        BookingResponseDto dto = new BookingResponseDto(
                1L, testStartTime, testEndTime, BookingStatus.CANCELED, booker, item
        );

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(testStartTime);
        assertThat(dto.getEnd()).isEqualTo(testEndTime);
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.CANCELED);
        assertThat(dto.getBooker()).isEqualTo(booker);
        assertThat(dto.getItem()).isEqualTo(item);
    }
}