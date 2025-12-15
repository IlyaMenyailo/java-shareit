package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .name("Test User")
                .email("test@example.com")
                .build();
    }

    @Test
    void createUser_shouldCreateUserSuccessfully() {
        UserDto result = userService.createUser(userDto);

        assertNotNull(result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void createUser_withDuplicateEmail_shouldThrowException() {
        userService.createUser(userDto);

        UserDto duplicateUser = UserDto.builder()
                .name("Another User")
                .email("test@example.com")
                .build();

        assertThrows(DuplicatedDataException.class,
                () -> userService.createUser(duplicateUser));
    }

    @Test
    void createUser_withEmptyEmail_shouldThrowException() {
        UserDto invalidUser = UserDto.builder()
                .name("Test")
                .email("")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(invalidUser));
    }

    @Test
    void updateUser_shouldUpdateUserSuccessfully() {
        UserDto created = userService.createUser(userDto);

        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        UserDto result = userService.updateUser(created.getId(), updateDto);

        assertEquals(created.getId(), result.getId());
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@example.com", result.getEmail());
    }

    @Test
    void updateUser_partialUpdate_shouldUpdateOnlyProvidedFields() {
        UserDto created = userService.createUser(userDto);

        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .build();

        UserDto result = userService.updateUser(created.getId(), updateDto);

        assertEquals(created.getId(), result.getId());
        assertEquals("Updated Name", result.getName());
        assertEquals("test@example.com", result.getEmail()); // Email остался прежним
    }

    @Test
    void updateUser_nonExistentUser_shouldThrowException() {
        UserDto updateDto = UserDto.builder()
                .name("Updated")
                .build();

        assertThrows(NotFoundException.class,
                () -> userService.updateUser(999L, updateDto));
    }

    @Test
    void getUserById_shouldReturnUser() {
        UserDto created = userService.createUser(userDto);

        UserDto result = userService.getUserById(created.getId());

        assertEquals(created.getId(), result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getUserById_nonExistentUser_shouldThrowException() {
        assertThrows(NotFoundException.class,
                () -> userService.getUserById(999L));
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        userService.createUser(userDto);

        UserDto anotherUser = UserDto.builder()
                .name("Another User")
                .email("another@example.com")
                .build();
        userService.createUser(anotherUser);

        List<UserDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getEmail().equals("test@example.com")));
        assertTrue(result.stream().anyMatch(u -> u.getEmail().equals("another@example.com")));
    }

    @Test
    void deleteUser_shouldDeleteUserSuccessfully() {
        UserDto created = userService.createUser(userDto);

        userService.deleteUser(created.getId());

        assertThrows(NotFoundException.class,
                () -> userService.getUserById(created.getId()));
    }

    @Test
    void deleteUser_nonExistentUser_shouldNotThrowException() {
        assertDoesNotThrow(() -> userService.deleteUser(999L));
    }
}