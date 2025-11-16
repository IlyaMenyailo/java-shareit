package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {

        if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }

        User user = UserMapper.toUser(userDto);
        User createdUser = userRepository.create(user);
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = userRepository.findUserById(userId);

        if (existingUser == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        if (userDto.getEmail() != null && !existingUser.getEmail().equals(userDto.getEmail())) {
            boolean emailExists = userRepository.findAll().stream()
                    .anyMatch(user -> user.getEmail() != null &&
                            user.getEmail().equalsIgnoreCase(userDto.getEmail()) &&
                            !user.getId().equals(userId));
            if (emailExists) {
                throw new DuplicatedDataException("Этот email уже используется");
            }
        }

        User updateUser = User.builder()
                .id(userId)
                .name(userDto.getName() != null ? userDto.getName() : existingUser.getName())
                .email(userDto.getEmail() != null ? userDto.getEmail() : existingUser.getEmail())
                .build();

        User updatedUser = userRepository.update(updateUser);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findUserById(userId);

        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.delete(userId);
    }
}