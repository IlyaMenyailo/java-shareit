package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    User create(User user);

    User update(User updateUser);

    List<User> findAll();

    User findUserById(Long id);

    void delete(Long id);
}