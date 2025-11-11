package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserRepository {
    User create(User user);

    User update(User updateUser);

    Collection<User> findAll();

    User findUserById(Long id);

    void delete(Long id);
}