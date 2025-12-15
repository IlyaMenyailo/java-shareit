package ru.practicum.server.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.server.user.model.User;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserForBookingDtoJsonTest {

    @Autowired
    private JacksonTester<UserForBookingDto> json;

    @Test
    void testSerialize() throws IOException {
        UserForBookingDto dto = UserForBookingDto.builder()
                .id(1L)
                .build();

        String jsonString = json.write(dto).getJson();

        assertThat(jsonString).contains("\"id\":1");
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = "{\"id\": 1}";

        UserForBookingDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void testToDtoMethod() {
        User user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        UserForBookingDto dto = UserForBookingDto.toDto(user);

        assertThat(dto.getId()).isEqualTo(1L);
    }

    @Test
    void testBuilder() {
        UserForBookingDto dto = UserForBookingDto.builder()
                .id(1L)
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
    }

    @Test
    void testNoArgsConstructor() {
        UserForBookingDto dto = new UserForBookingDto();
        dto.setId(1L);

        assertThat(dto.getId()).isEqualTo(1L);
    }

    @Test
    void testAllArgsConstructor() {
        UserForBookingDto dto = new UserForBookingDto(1L);

        assertThat(dto.getId()).isEqualTo(1L);
    }
}