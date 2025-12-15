package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

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
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("User1")
                .email("user1@yandex.ru")
                .build();

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("User1");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("user1@yandex.ru");
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = "{\"id\": 1, \"name\": \"User1\", \"email\": \"user1@yandex.ru\"}";

        UserDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("User1");
        assertThat(result.getEmail()).isEqualTo("user1@yandex.ru");
    }

    @Test
    void testDeserialize_partialData() throws IOException {
        String jsonContent = "{\"name\": \"User1\", \"email\": \"user1@yandex.ru\"}";

        UserDto result = json.parse(jsonContent).getObject();

        assertThat(result.getName()).isEqualTo("User1");
        assertThat(result.getEmail()).isEqualTo("user1@yandex.ru");
        assertThat(result.getId()).isNull();
    }

    @Test
    void testValidation_validUser() {
        UserDto userDto = UserDto.builder()
                .name("Valid User")
                .email("valid@example.com")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_emptyName() {
        UserDto userDto = UserDto.builder()
                .name("")
                .email("test@example.com")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")
                        && v.getMessage().contains("не может быть пустым")));
    }

    @Test
    void testValidation_nullEmail() {
        UserDto userDto = UserDto.builder()
                .name("Test User")
                .email(null)
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")
                        && v.getMessage().contains("не может быть пустым")));
    }

    @Test
    void testValidation_invalidEmailFormat() {
        UserDto userDto = UserDto.builder()
                .name("Test User")
                .email("invalid-email")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")
                        && v.getMessage().contains("Некорректный формат email")));
    }

    @Test
    void testValidation_emptyEmail() {
        UserDto userDto = UserDto.builder()
                .name("Test User")
                .email("")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertFalse(violations.isEmpty());
        boolean hasNotBlankViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")
                        && v.getMessage().contains("не может быть пустым"));
        boolean hasEmailViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")
                        && v.getMessage().contains("Некорректный формат email"));

        assertTrue(hasNotBlankViolation || hasEmailViolation);
    }

    @Test
    void testBuilder() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("Builder Test")
                .email("builder@test.com")
                .build();

        assertEquals(1L, userDto.getId());
        assertEquals("Builder Test", userDto.getName());
        assertEquals("builder@test.com", userDto.getEmail());
    }
}