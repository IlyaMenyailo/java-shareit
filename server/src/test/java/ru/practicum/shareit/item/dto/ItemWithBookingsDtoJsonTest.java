package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemWithBookingsDtoJsonTest {

    @Autowired
    private JacksonTester<ItemWithBookingsDto> json;

    private ObjectMapper objectMapper;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        testTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
    }

    @Test
    void testSerialize() throws IOException {
        BookingShortDto lastBooking = BookingShortDto.builder()
                .id(1L)
                .bookerId(10L)
                .start(testTime.minusDays(2))
                .end(testTime.minusDays(1))
                .build();

        BookingShortDto nextBooking = BookingShortDto.builder()
                .id(2L)
                .bookerId(11L)
                .start(testTime.plusDays(1))
                .end(testTime.plusDays(2))
                .build();

        CommentDto comment1 = CommentDto.builder()
                .id(1L)
                .text("Great item!")
                .authorName("User1")
                .created(testTime.minusDays(3))
                .build();

        CommentDto comment2 = CommentDto.builder()
                .id(2L)
                .text("Works perfect")
                .authorName("User2")
                .created(testTime.minusDays(1))
                .build();

        ItemWithBookingsDto dto = ItemWithBookingsDto.builder()
                .id(1L)
                .name("Drill")
                .description("Professional drill")
                .available(true)
                .request(10L)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(Arrays.asList(comment1, comment2))
                .build();

        JsonContent<ItemWithBookingsDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Drill");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Professional drill");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.request").isEqualTo(10);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(2);
        assertThat(result).extractingJsonPathArrayValue("$.comments").hasSize(2);
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = "{" +
                "\"id\": 1," +
                "\"name\": \"Hammer\"," +
                "\"description\": \"Heavy hammer\"," +
                "\"available\": true," +
                "\"request\": 5," +
                "\"lastBooking\": {\"id\": 1, \"bookerId\": 10}," +
                "\"nextBooking\": {\"id\": 2, \"bookerId\": 11}," +
                "\"comments\": [{\"id\": 1, \"text\": \"Good\", \"authorName\": \"User1\"}]" +
                "}";

        ItemWithBookingsDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Hammer");
        assertThat(result.getDescription()).isEqualTo("Heavy hammer");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getRequest()).isEqualTo(5L);
        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getLastBooking().getId()).isEqualTo(1L);
        assertThat(result.getNextBooking()).isNotNull();
        assertThat(result.getNextBooking().getId()).isEqualTo(2L);
        assertThat(result.getComments()).hasSize(1);
    }

    @Test
    void testDeserialize_withOptionalFieldsMissing() throws IOException {
        String jsonContent = "{" +
                "\"id\": 1," +
                "\"name\": \"Item\"," +
                "\"description\": \"Description\"," +
                "\"available\": true" +
                "}";

        ItemWithBookingsDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Item");
        assertThat(result.getDescription()).isEqualTo("Description");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getRequest()).isNull();
        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
        assertThat(result.getComments()).isNull();
    }

    @Test
    void testBuilder() {
        BookingShortDto booking = BookingShortDto.builder().id(1L).build();
        CommentDto comment = CommentDto.builder().id(1L).text("Test").build();

        ItemWithBookingsDto dto = ItemWithBookingsDto.builder()
                .id(1L)
                .name("Builder Test")
                .description("Builder description")
                .available(true)
                .request(10L)
                .lastBooking(booking)
                .nextBooking(booking)
                .comments(List.of(comment))
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Builder Test");
        assertThat(dto.getDescription()).isEqualTo("Builder description");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequest()).isEqualTo(10L);
        assertThat(dto.getLastBooking()).isEqualTo(booking);
        assertThat(dto.getNextBooking()).isEqualTo(booking);
        assertThat(dto.getComments()).containsExactly(comment);
    }

    @Test
    void testNoArgsConstructor() {
        ItemWithBookingsDto dto = new ItemWithBookingsDto();
        dto.setId(1L);
        dto.setName("Setter Test");
        dto.setDescription("Setter Description");
        dto.setAvailable(true);
        dto.setRequest(10L);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Setter Test");
        assertThat(dto.getDescription()).isEqualTo("Setter Description");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequest()).isEqualTo(10L);
    }

    @Test
    void testAllArgsConstructor() {
        BookingShortDto booking = BookingShortDto.builder().id(1L).build();
        List<CommentDto> comments = List.of(CommentDto.builder().id(1L).build());

        ItemWithBookingsDto dto = new ItemWithBookingsDto(
                1L, "All Args", "All args description", true, 10L,
                booking, booking, comments
        );

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("All Args");
        assertThat(dto.getDescription()).isEqualTo("All args description");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequest()).isEqualTo(10L);
        assertThat(dto.getLastBooking()).isEqualTo(booking);
        assertThat(dto.getNextBooking()).isEqualTo(booking);
        assertThat(dto.getComments()).isEqualTo(comments);
    }
}