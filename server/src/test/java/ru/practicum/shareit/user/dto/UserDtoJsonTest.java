package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
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

    @Test
    void testAllArgsConstructor() {
        UserDto userDto = new UserDto(1L, "All Args", "allargs@example.com");

        assertEquals(1L, userDto.getId());
        assertEquals("All Args", userDto.getName());
        assertEquals("allargs@example.com", userDto.getEmail());
    }

    @Test
    void testSettersAndGetters() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Setter Test");
        userDto.setEmail("setter@test.com");

        assertEquals(1L, userDto.getId());
        assertEquals("Setter Test", userDto.getName());
        assertEquals("setter@test.com", userDto.getEmail());
    }
}