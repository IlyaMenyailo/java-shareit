package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.io.IOException;
import java.time.LocalDateTime;
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
    void testSerialization() throws IOException {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 10, 0, 0);

        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .itemId(100L)
                .build();

        String jsonString = json.write(bookingDto).getJson();

        assertThat(jsonString).contains("\"start\":\"2024-01-01T10:00:00\"");
        assertThat(jsonString).contains("\"end\":\"2024-01-02T10:00:00\"");
        assertThat(jsonString).contains("\"itemId\":100");
    }

    @Test
    void testDeserialization() throws IOException {
        String jsonContent = "{" +
                "\"start\": \"2024-01-01T10:00:00\"," +
                "\"end\": \"2024-01-02T10:00:00\"," +
                "\"itemId\": 100" +
                "}";

        BookingDto result = json.parse(jsonContent).getObject();

        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 2, 10, 0, 0));
        assertThat(result.getItemId()).isEqualTo(100L);
    }

    @Test
    void testValidation() {
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 10, 0, 0);  // Прошлое время
        LocalDateTime end = LocalDateTime.of(2023, 1, 2, 10, 0, 0);    // Прошлое время

        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(null)  // null должен вызвать ошибку валидации
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);

        assertFalse(violations.isEmpty());

        boolean hasItemIdError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("itemId")
                        && v.getMessage().contains("не может быть пустым"));
        assertTrue(hasItemIdError);
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
}