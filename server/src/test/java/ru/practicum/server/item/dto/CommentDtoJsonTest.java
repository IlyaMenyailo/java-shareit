package ru.practicum.server.item.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    private ObjectMapper objectMapper;
    private Validator validator;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        testTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
    }

    @Test
    void testSerialize() throws IOException {
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("message about item1")
                .authorName("user1")
                .created(testTime)
                .build();

        JsonContent<CommentDto> result = json.write(commentDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("message about item1");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("user1");

        String expectedDate = testTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(expectedDate);
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = String.format(
                "{\"id\": 1, \"text\": \"Great item2!\", \"authorName\": \"user2\", \"created\": \"%s\"}",
                testTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

        CommentDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Great item2");
        assertThat(result.getAuthorName()).isEqualTo("user2");
        assertThat(result.getCreated()).isEqualTo(testTime);
    }

    @Test
    void testDeserialize_withoutOptionalFields() throws IOException {
        String jsonContent = String.format(
                "{\"text\": \"Simple comment\", \"created\": \"%s\"}",
                testTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

        CommentDto result = json.parse(jsonContent).getObject();

        assertThat(result.getText()).isEqualTo("Simple comment");
        assertThat(result.getCreated()).isEqualTo(testTime);
        assertThat(result.getId()).isNull();
        assertThat(result.getAuthorName()).isNull();
    }

    @Test
    void testValidation_validComment() {
        CommentDto commentDto = CommentDto.builder()
                .text("Valid comment text")
                .created(testTime)
                .build();

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_emptyText() {
        CommentDto commentDto = CommentDto.builder()
                .text("")
                .created(testTime)
                .build();

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("text")
                        && v.getMessage().contains("не может быть пустым")));
    }

    @Test
    void testValidation_nullText() {
        CommentDto commentDto = CommentDto.builder()
                .text(null)
                .created(testTime)
                .build();

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("text")
                        && v.getMessage().contains("не может быть пустым")));
    }

    @Test
    void testValidation_blankText() {
        CommentDto commentDto = CommentDto.builder()
                .text("   ")
                .created(testTime)
                .build();

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("text")
                        && v.getMessage().contains("не может быть пустым")));
    }

    @Test
    void testValidation_longText() {
        String longText = "A".repeat(1000);
        CommentDto commentDto = CommentDto.builder()
                .text(longText)
                .created(testTime)
                .build();

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertTrue(violations.isEmpty()); // No max length constraint
    }

    @Test
    void testJsonDateFormat() throws IOException {
        String jsonWithDifferentDateFormat = "{" +
                "\"text\": \"Test\"," +
                "\"created\": \"2024-01-01T12:00:00\"" +
                "}";

        CommentDto result = objectMapper.readValue(jsonWithDifferentDateFormat, CommentDto.class);

        assertThat(result.getCreated()).isEqualTo(testTime);
    }

    @Test
    void testBuilder() {
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Builder test comment")
                .authorName("Builder Author")
                .created(testTime)
                .build();

        assertThat(commentDto.getId()).isEqualTo(1L);
        assertThat(commentDto.getText()).isEqualTo("Builder test comment");
        assertThat(commentDto.getAuthorName()).isEqualTo("Builder Author");
        assertThat(commentDto.getCreated()).isEqualTo(testTime);
    }

    @Test
    void testNoArgsConstructor() {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Setter test");
        commentDto.setAuthorName("Setter Author");
        commentDto.setCreated(testTime);

        assertThat(commentDto.getId()).isEqualTo(1L);
        assertThat(commentDto.getText()).isEqualTo("Setter test");
        assertThat(commentDto.getAuthorName()).isEqualTo("Setter Author");
        assertThat(commentDto.getCreated()).isEqualTo(testTime);
    }

    @Test
    void testAllArgsConstructor() {
        CommentDto commentDto = new CommentDto(1L, "All args text", "All Args Author", testTime);

        assertThat(commentDto.getId()).isEqualTo(1L);
        assertThat(commentDto.getText()).isEqualTo("All args text");
        assertThat(commentDto.getAuthorName()).isEqualTo("All Args Author");
        assertThat(commentDto.getCreated()).isEqualTo(testTime);
    }
}