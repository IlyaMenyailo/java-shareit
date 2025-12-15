package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;
    private User user;
    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        userDto = UserDto.builder()
                .id(userId)
                .name("user1")
                .email("user1.doe@example.com")
                .build();

        user = User.builder()
                .id(userId)
                .name("user1")
                .email("user1.doe@example.com")
                .build();
    }

    @Test
    void createUser_shouldCreateUserSuccessfully() {
        when(userRepository.findAll()).thenReturn(List.of());
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_withEmptyEmail_shouldThrowIllegalArgumentException() {
        UserDto invalidUserDto = UserDto.builder()
                .name("user1")
                .email("   ")
                .build();

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(invalidUserDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_withDuplicatedEmail_shouldThrowDuplicatedDataException() {
        User existingUser = User.builder()
                .id(2L)
                .name("user2")
                .email("user1.doe@example.com")
                .build();

        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        assertThrows(DuplicatedDataException.class, () -> userService.createUser(userDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(userId, userDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_withDuplicatedEmail_shouldThrowDuplicatedDataException() {
        User existingUser = User.builder()
                .id(userId)
                .name("user1")
                .email("user1@example.com")
                .build();

        User anotherUser = User.builder()
                .id(2L)
                .name("user2")
                .email("user2@example.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findAll()).thenReturn(List.of(existingUser, anotherUser));

        UserDto updateDto = UserDto.builder()
                .email("user2@example.com")
                .build();

        assertThrows(DuplicatedDataException.class, () -> userService.updateUser(userId, updateDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_shouldReturnUserSuccessfully() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
    }

    @Test
    void getUserById_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        List<User> users = List.of(
                user,
                User.builder().id(2L).name("user3").email("user3@example.com").build()
        );

        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getName());
        assertEquals("user3", result.get(1).getName());
    }

    @Test
    void deleteUser_shouldDeleteUserSuccessfully() {
        doNothing().when(userRepository).deleteById(userId);

        assertDoesNotThrow(() -> userService.deleteUser(userId));
        verify(userRepository, times(1)).deleteById(userId);
    }
}