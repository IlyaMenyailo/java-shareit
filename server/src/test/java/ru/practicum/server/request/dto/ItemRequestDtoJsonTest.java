package ru.practicum.server.request.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    private ItemRequestDto itemRequestDto;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(testTime)
                .items(Collections.emptyList())
                .build();
    }

    @Test
    void testSerialize() throws IOException {
        JsonContent<ItemRequestDto> result = json.write(itemRequestDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Need a drill");

        String expectedDate = testTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(expectedDate);
        assertThat(result).extractingJsonPathArrayValue("$.items").isEmpty();
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = String.format(
                "{\"id\": 1, \"description\": \"Need a drill\", \"created\": \"%s\", \"items\": []}",
                testTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

        ItemRequestDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Need a drill");
        assertThat(result.getCreated()).isEqualTo(testTime);
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void testValidationConstraints() {
        ItemRequestDto invalidDto = ItemRequestDto.builder()
                .id(1L)
                .description("")
                .created(testTime)
                .build();
    }
}