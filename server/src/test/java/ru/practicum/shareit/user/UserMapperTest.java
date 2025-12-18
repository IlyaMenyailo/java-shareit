package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toUserDto_shouldConvertUserToDto() {
        User user = User.builder()
                .id(1L)
                .name("user1")
                .email("user1@example.com")
                .build();

        UserDto dto = UserMapper.toUserDto(user);

        assertNotNull(dto);
        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getName(), dto.getName());
        assertEquals(user.getEmail(), dto.getEmail());
    }

    @Test
    void toUser_shouldConvertDtoToUser() {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("user1")
                .email("user1@example.com")
                .build();

        User user = UserMapper.toUser(dto);

        assertNotNull(user);
        assertEquals(dto.getId(), user.getId());
        assertEquals(dto.getName(), user.getName());
        assertEquals(dto.getEmail(), user.getEmail());
    }

    @Test
    void toUser_withNullId_shouldSetNullId() {
        UserDto dto = UserDto.builder()
                .name("user1")
                .email("user1@example.com")
                .build();

        User user = UserMapper.toUser(dto);

        assertNull(user.getId());
        assertEquals(dto.getName(), user.getName());
        assertEquals(dto.getEmail(), user.getEmail());
    }

    @Test
    void toUserDto_withNullUser_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> UserMapper.toUserDto(null));
    }

    @Test
    void toUser_withNullDto_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> UserMapper.toUser(null));
    }

    @Test
    void conversionRoundTrip_shouldReturnSameValues() {
        User original = User.builder()
                .id(1L)
                .name("Original")
                .email("original@example.com")
                .build();

        UserDto dto = UserMapper.toUserDto(original);
        User converted = UserMapper.toUser(dto);

        assertEquals(original.getId(), converted.getId());
        assertEquals(original.getName(), converted.getName());
        assertEquals(original.getEmail(), converted.getEmail());
    }
}