package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestShortDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestShortDto> json;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSerialize() throws IOException {
        ItemRequestShortDto dto = ItemRequestShortDto.builder()
                .description("Need a drill for home")
                .build();

        String jsonString = json.write(dto).getJson();

        assertThat(jsonString).contains("\"description\":\"Need a drill for home\"");
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = "{\"description\": \"Looking for a ladder\"}";

        ItemRequestShortDto result = json.parse(jsonContent).getObject();

        assertThat(result.getDescription()).isEqualTo("Looking for a ladder");
    }

    @Test
    void testBuilder() {
        ItemRequestShortDto dto = ItemRequestShortDto.builder()
                .description("Builder test description")
                .build();

        assertThat(dto.getDescription()).isEqualTo("Builder test description");
    }

    @Test
    void testNoArgsConstructor() {
        ItemRequestShortDto dto = new ItemRequestShortDto();
        dto.setDescription("Setter test description");

        assertThat(dto.getDescription()).isEqualTo("Setter test description");
    }

    @Test
    void testAllArgsConstructor() {
        ItemRequestShortDto dto = new ItemRequestShortDto("All args description");

        assertThat(dto.getDescription()).isEqualTo("All args description");
    }

    @Test
    void testSettersAndGetters() {
        ItemRequestShortDto dto = new ItemRequestShortDto();
        dto.setDescription("Getter/Setter test");

        assertThat(dto.getDescription()).isEqualTo("Getter/Setter test");
    }
}