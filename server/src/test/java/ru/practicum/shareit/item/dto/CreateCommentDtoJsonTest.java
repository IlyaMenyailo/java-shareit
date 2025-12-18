package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CreateCommentDtoJsonTest {

    @Autowired
    private JacksonTester<CreateCommentDto> json;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSerialize() throws IOException {
        CreateCommentDto dto = CreateCommentDto.builder()
                .text("This is a test comment about the item.")
                .build();

        String jsonString = json.write(dto).getJson();

        assertThat(jsonString).contains("\"text\":\"This is a test comment about the item.\"");
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = "{\"text\": \"Great product, it works perfect!\"}";

        CreateCommentDto result = json.parse(jsonContent).getObject();

        assertThat(result.getText()).isEqualTo("Great product, it works perfect!");
    }

    @Test
    void testBuilder() {
        CreateCommentDto dto = CreateCommentDto.builder()
                .text("Builder test comment")
                .build();

        assertThat(dto.getText()).isEqualTo("Builder test comment");
    }

    @Test
    void testNoArgsConstructor() {
        CreateCommentDto dto = new CreateCommentDto();
        dto.setText("Setter test comment");

        assertThat(dto.getText()).isEqualTo("Setter test comment");
    }

    @Test
    void testAllArgsConstructor() {
        CreateCommentDto dto = new CreateCommentDto("All args comment");

        assertThat(dto.getText()).isEqualTo("All args comment");
    }

    @Test
    void testSettersAndGetters() {
        CreateCommentDto dto = new CreateCommentDto();
        dto.setText("Getter/Setter test");

        assertThat(dto.getText()).isEqualTo("Getter/Setter test");
    }
}