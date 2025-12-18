package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSerialize() throws IOException {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Power Drill")
                .description("Professional drill")
                .available(true)
                .requestId(10L)
                .ownerId(5L)
                .build();

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Power Drill");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Professional drill");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(10);
        assertThat(result).extractingJsonPathNumberValue("$.ownerId").isEqualTo(5);
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = "{" +
                "\"id\": 1," +
                "\"name\": \"Hammer\"," +
                "\"description\": \"Heavy hammer\"," +
                "\"available\": false," +
                "\"requestId\": 20," +
                "\"ownerId\": 3" +
                "}";

        ItemDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Hammer");
        assertThat(result.getDescription()).isEqualTo("Heavy hammer");
        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getRequestId()).isEqualTo(20L);
        assertThat(result.getOwnerId()).isEqualTo(3L);
    }

    @Test
    void testDeserialize_partialData() throws IOException {
        String jsonContent = "{" +
                "\"name\": \"Updated Item\"," +
                "\"description\": \"Updated description\"" +
                "}";

        ItemDto result = json.parse(jsonContent).getObject();

        assertThat(result.getName()).isEqualTo("Updated Item");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.getId()).isNull();
        assertThat(result.getAvailable()).isNull();
        assertThat(result.getRequestId()).isNull();
        assertThat(result.getOwnerId()).isNull();
    }

    @Test
    void testBuilder() {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Builder Test")
                .description("Builder description")
                .available(false)
                .requestId(100L)
                .ownerId(50L)
                .build();

        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Builder Test");
        assertThat(itemDto.getDescription()).isEqualTo("Builder description");
        assertThat(itemDto.getAvailable()).isFalse();
        assertThat(itemDto.getRequestId()).isEqualTo(100L);
        assertThat(itemDto.getOwnerId()).isEqualTo(50L);
    }

    @Test
    void testAllArgsConstructor() {
        ItemDto itemDto = new ItemDto(1L, "All Args", "All args description", true, 10L, 5L);

        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("All Args");
        assertThat(itemDto.getDescription()).isEqualTo("All args description");
        assertThat(itemDto.getAvailable()).isTrue();
        assertThat(itemDto.getRequestId()).isEqualTo(10L);
        assertThat(itemDto.getOwnerId()).isEqualTo(5L);
    }

    @Test
    void testSettersAndGetters() {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Setter Test");
        itemDto.setDescription("Setter Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(10L);
        itemDto.setOwnerId(5L);

        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Setter Test");
        assertThat(itemDto.getDescription()).isEqualTo("Setter Description");
        assertThat(itemDto.getAvailable()).isTrue();
        assertThat(itemDto.getRequestId()).isEqualTo(10L);
        assertThat(itemDto.getOwnerId()).isEqualTo(5L);
    }
}