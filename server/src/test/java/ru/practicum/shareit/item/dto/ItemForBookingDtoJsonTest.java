package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.model.Item;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemForBookingDtoJsonTest {

    @Autowired
    private JacksonTester<ItemForBookingDto> json;

    @Test
    void testSerialize() throws IOException {
        ItemForBookingDto dto = ItemForBookingDto.builder()
                .id(1L)
                .name("Power Drill")
                .build();

        String jsonString = json.write(dto).getJson();

        assertThat(jsonString).contains("\"id\":1");
        assertThat(jsonString).contains("\"name\":\"Power Drill\"");
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = "{\"id\": 1, \"name\": \"Hammer\"}";

        ItemForBookingDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Hammer");
    }

    @Test
    void testToDtoMethod() {
        Item item = Item.builder()
                .id(1L)
                .name("Test Item")
                .description("Description")
                .available(true)
                .owner(10L)
                .build();

        ItemForBookingDto dto = ItemForBookingDto.toDto(item);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Test Item");
    }

    @Test
    void testBuilder() {
        ItemForBookingDto dto = ItemForBookingDto.builder()
                .id(1L)
                .name("Builder Test")
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Builder Test");
    }

    @Test
    void testNoArgsConstructor() {
        ItemForBookingDto dto = new ItemForBookingDto();
        dto.setId(1L);
        dto.setName("Setter Test");

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Setter Test");
    }

    @Test
    void testAllArgsConstructor() {
        ItemForBookingDto dto = new ItemForBookingDto(1L, "All Args");

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("All Args");
    }
}