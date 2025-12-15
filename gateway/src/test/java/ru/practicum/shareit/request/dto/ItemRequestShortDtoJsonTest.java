package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class ItemRequestShortDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestShortDto> json;

    private ObjectMapper objectMapper;
    private Validator validator;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testSerialize() throws IOException {
        ItemRequestShortDto dto = ItemRequestShortDto.builder()
                .description("Need a drill for home repairs")
                .build();

        String jsonString = json.write(dto).getJson();

        assertThat(jsonString).contains("\"description\":\"Need a drill for home repairs\"");
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = "{\"description\": \"Need a hammer\"}";

        ItemRequestShortDto result = json.parse(jsonContent).getObject();

        assertThat(result.getDescription()).isEqualTo("Need a hammer");
    }

    @Test
    void testValidation_validDto() {
        ItemRequestShortDto dto = ItemRequestShortDto.builder()
                .description("Valid description")
                .build();

        Set<ConstraintViolation<ItemRequestShortDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_emptyDescription() {
        ItemRequestShortDto dto = ItemRequestShortDto.builder()
                .description("")
                .build();

        Set<ConstraintViolation<ItemRequestShortDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("description")
                        && v.getMessage().contains("не может быть пустым")));
    }

    @Test
    void testValidation_nullDescription() {
        ItemRequestShortDto dto = ItemRequestShortDto.builder()
                .description(null)
                .build();

        Set<ConstraintViolation<ItemRequestShortDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("description")
                        && v.getMessage().contains("не может быть пустым")));
    }

    @Test
    void testValidation_blankDescription() {
        ItemRequestShortDto dto = ItemRequestShortDto.builder()
                .description("   ")
                .build();

        Set<ConstraintViolation<ItemRequestShortDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("description")
                        && v.getMessage().contains("не может быть пустым")));
    }

    @Test
    void testBuilderPattern() {
        ItemRequestShortDto dto = ItemRequestShortDto.builder()
                .description("Test description")
                .build();

        assertThat(dto.getDescription()).isEqualTo("Test description");
    }

    @Test
    void testAllArgsConstructor() {
        ItemRequestShortDto dto = new ItemRequestShortDto("Constructor test");

        assertThat(dto.getDescription()).isEqualTo("Constructor test");
    }

    @Test
    void testNoArgsConstructor() {
        ItemRequestShortDto dto = new ItemRequestShortDto();
        dto.setDescription("Setter test");

        assertThat(dto.getDescription()).isEqualTo("Setter test");
    }
}