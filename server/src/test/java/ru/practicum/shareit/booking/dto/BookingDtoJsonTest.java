package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.BookingStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

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
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(testStartTime)
                .end(testEndTime)
                .itemId(100L)
                .status(BookingStatus.WAITING)
                .build();

        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);

        String expectedStart = testStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        String expectedEnd = testEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(expectedStart);
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(expectedEnd);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(100);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = String.format(
                "{\"id\": 1, \"start\": \"%s\", \"end\": \"%s\", \"itemId\": 100, \"status\": \"APPROVED\"}",
                testStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                testEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

        BookingDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStart()).isEqualTo(testStartTime);
        assertThat(result.getEnd()).isEqualTo(testEndTime);
        assertThat(result.getItemId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void testDeserialize_withoutOptionalFields() throws IOException {
        String jsonContent = String.format(
                "{\"start\": \"%s\", \"end\": \"%s\", \"itemId\": 100}",
                testStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                testEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

        BookingDto result = json.parse(jsonContent).getObject();

        assertThat(result.getStart()).isEqualTo(testStartTime);
        assertThat(result.getEnd()).isEqualTo(testEndTime);
        assertThat(result.getItemId()).isEqualTo(100L);
        assertThat(result.getId()).isNull();
        assertThat(result.getStatus()).isNull();
    }

    @Test
    void testJsonDateFormat() throws IOException {
        String jsonWithDate = String.format(
                "{\"start\": \"%s\", \"end\": \"%s\", \"itemId\": 100}",
                "2024-01-01T10:00:00",
                "2024-01-02T10:00:00");

        BookingDto result = objectMapper.readValue(jsonWithDate, BookingDto.class);

        assertThat(result.getStart()).isEqualTo(testStartTime);
        assertThat(result.getEnd()).isEqualTo(testEndTime);
    }

    @Test
    void testBuilder() {
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(testStartTime)
                .end(testEndTime)
                .itemId(100L)
                .status(BookingStatus.WAITING)
                .build();

        assertThat(bookingDto.getId()).isEqualTo(1L);
        assertThat(bookingDto.getStart()).isEqualTo(testStartTime);
        assertThat(bookingDto.getEnd()).isEqualTo(testEndTime);
        assertThat(bookingDto.getItemId()).isEqualTo(100L);
        assertThat(bookingDto.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void testNoArgsConstructor() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(testStartTime);
        bookingDto.setEnd(testEndTime);
        bookingDto.setItemId(100L);
        bookingDto.setStatus(BookingStatus.APPROVED);

        assertThat(bookingDto.getId()).isEqualTo(1L);
        assertThat(bookingDto.getStart()).isEqualTo(testStartTime);
        assertThat(bookingDto.getEnd()).isEqualTo(testEndTime);
        assertThat(bookingDto.getItemId()).isEqualTo(100L);
        assertThat(bookingDto.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void testAllArgsConstructor() {
        BookingDto bookingDto = new BookingDto(
                1L, testStartTime, testEndTime, 100L, BookingStatus.REJECTED
        );

        assertThat(bookingDto.getId()).isEqualTo(1L);
        assertThat(bookingDto.getStart()).isEqualTo(testStartTime);
        assertThat(bookingDto.getEnd()).isEqualTo(testEndTime);
        assertThat(bookingDto.getItemId()).isEqualTo(100L);
        assertThat(bookingDto.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void testDifferentStatusValues() throws IOException {
        for (BookingStatus status : BookingStatus.values()) {
            String jsonContent = String.format(
                    "{\"start\": \"%s\", \"end\": \"%s\", \"itemId\": 100, \"status\": \"%s\"}",
                    testStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                    testEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                    status.name());

            BookingDto result = json.parse(jsonContent).getObject();
            assertThat(result.getStatus()).isEqualTo(status);
        }
    }
}