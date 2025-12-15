package ru.practicum.server.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.BookingStatus;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    private ObjectMapper objectMapper;
    private Validator validator;
    private LocalDateTime testStartTime;
    private LocalDateTime testEndTime;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

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
    void testValidation_nullStart() {
        BookingDto bookingDto = BookingDto.builder()
                .start(null)
                .end(testEndTime)
                .itemId(100L)
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("start")
                        && v.getMessage().contains("не может быть пустой")));
    }

    @Test
    void testValidation_pastStart() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .itemId(100L)
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("start")
                        && v.getMessage().contains("должна быть в настоящем или будущем")));
    }

    @Test
    void testValidation_nullEnd() {
        BookingDto bookingDto = BookingDto.builder()
                .start(testStartTime)
                .end(null)
                .itemId(100L)
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("end")
                        && v.getMessage().contains("не может быть пустой")));
    }

    @Test
    void testValidation_pastEnd() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().minusDays(1))
                .itemId(100L)
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("end")
                        && v.getMessage().contains("должна быть в будущем")));
    }

    @Test
    void testValidation_presentEnd() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now())
                .itemId(100L)
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("end")
                        && v.getMessage().contains("должна быть в будущем")));
    }

    @Test
    void testValidation_nullItemId() {
        BookingDto bookingDto = BookingDto.builder()
                .start(testStartTime)
                .end(testEndTime)
                .itemId(null)
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("itemId")
                        && v.getMessage().contains("не может быть пустым")));
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