package ru.practicum.shareit.item.dto;

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
                .text("item1")
                .authorName("Name1")
                .created(testTime)
                .build();

        String jsonString = json.write(commentDto).getJson();

        assertThat(jsonString).contains("\"id\":1");
        assertThat(jsonString).contains("\"text\":\"item1\"");
        assertThat(jsonString).contains("\"authorName\":\"Name1\"");

        String expectedDate = testTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        assertThat(jsonString).contains("\"created\":\"" + expectedDate + "\"");
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = String.format(
                "{\"id\": 1, \"text\": \"Item2\", \"authorName\": \"Name2\", \"created\": \"%s\"}",
                testTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

        CommentDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Item2");
        assertThat(result.getAuthorName()).isEqualTo("Name2");
        assertThat(result.getCreated()).isEqualTo(testTime);
    }

    @Test
    void testValidation_validComment() {
        CommentDto commentDto = CommentDto.builder()
                .text("Valid comment")
                .created(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_emptyText() {
        CommentDto commentDto = CommentDto.builder()
                .text("")
                .created(LocalDateTime.now())
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
                .created(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("text")
                        && v.getMessage().contains("не может быть пустым")));
    }

    @Test
    void testDateFormat() throws IOException {
        String jsonWithDate = "{\"text\": \"Test\", \"created\": \"2024-01-01T12:00:00\"}";

        CommentDto result = objectMapper.readValue(jsonWithDate, CommentDto.class);

        assertThat(result.getCreated()).isEqualTo(testTime);
    }

    @Test
    void testBuilder() {
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Builder test")
                .authorName("Builder")
                .created(testTime)
                .build();

        assertThat(commentDto.getId()).isEqualTo(1L);
        assertThat(commentDto.getText()).isEqualTo("Builder test");
        assertThat(commentDto.getAuthorName()).isEqualTo("Builder");
        assertThat(commentDto.getCreated()).isEqualTo(testTime);
    }

    @Test
    void testNoArgsConstructor() {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Setter test");
        commentDto.setAuthorName("Setter");
        commentDto.setCreated(testTime);

        assertThat(commentDto.getId()).isEqualTo(1L);
        assertThat(commentDto.getText()).isEqualTo("Setter test");
        assertThat(commentDto.getAuthorName()).isEqualTo("Setter");
        assertThat(commentDto.getCreated()).isEqualTo(testTime);
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
}