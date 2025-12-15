package ru.practicum.server.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingShortDtoJsonTest {

    @Autowired
    private JacksonTester<BookingShortDto> json;

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
        BookingShortDto dto = BookingShortDto.builder()
                .id(1L)
                .bookerId(10L)
                .start(testStartTime)
                .end(testEndTime)
                .build();

        String jsonString = json.write(dto).getJson();

        assertThat(jsonString).contains("\"id\":1");
        assertThat(jsonString).contains("\"bookerId\":10");
        assertThat(jsonString).contains("\"start\":\"2024-01-01T10:00:00\"");
        assertThat(jsonString).contains("\"end\":\"2024-01-02T10:00:00\"");
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = "{" +
                "\"id\": 1," +
                "\"bookerId\": 10," +
                "\"start\": \"2024-01-01T10:00:00\"," +
                "\"end\": \"2024-01-02T10:00:00\"" +
                "}";

        BookingShortDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBookerId()).isEqualTo(10L);
        assertThat(result.getStart()).isEqualTo(testStartTime);
        assertThat(result.getEnd()).isEqualTo(testEndTime);
    }

    @Test
    void testDeserialize_partialData() throws IOException {
        String jsonContent = "{" +
                "\"id\": 1," +
                "\"bookerId\": 10" +
                "}";

        BookingShortDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBookerId()).isEqualTo(10L);
        assertThat(result.getStart()).isNull();
        assertThat(result.getEnd()).isNull();
    }

    @Test
    void testBuilder() {
        BookingShortDto dto = BookingShortDto.builder()
                .id(1L)
                .bookerId(10L)
                .start(testStartTime)
                .end(testEndTime)
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getBookerId()).isEqualTo(10L);
        assertThat(dto.getStart()).isEqualTo(testStartTime);
        assertThat(dto.getEnd()).isEqualTo(testEndTime);
    }

    @Test
    void testNoArgsConstructor() {
        BookingShortDto dto = new BookingShortDto();
        dto.setId(1L);
        dto.setBookerId(10L);
        dto.setStart(testStartTime);
        dto.setEnd(testEndTime);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getBookerId()).isEqualTo(10L);
        assertThat(dto.getStart()).isEqualTo(testStartTime);
        assertThat(dto.getEnd()).isEqualTo(testEndTime);
    }

    @Test
    void testAllArgsConstructor() {
        BookingShortDto dto = new BookingShortDto(1L, 10L, testStartTime, testEndTime);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getBookerId()).isEqualTo(10L);
        assertThat(dto.getStart()).isEqualTo(testStartTime);
        assertThat(dto.getEnd()).isEqualTo(testEndTime);
    }
}