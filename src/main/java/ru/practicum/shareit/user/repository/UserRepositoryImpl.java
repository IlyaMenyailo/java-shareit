package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
@Slf4j
public class UserRepositoryImpl implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();

    @Override
    public User create(User user) {
        log.info("Попытка создать пользователя с email: {}", user.getEmail());
        validateEmail(user.getEmail(), null);
        user.setId(getNextId());
        users.put(user.getId(), user);
        emails.add(user.getEmail().toLowerCase());
        log.info("Пользователь с id {} и email {} успешно создан", user.getId(), user.getEmail());
        return user;
    }

    @Override
    public User update(User updateUser) {
        log.info("Попытка обновить пользователя с id: {}", updateUser.getId());
        User existingUser = users.get(updateUser.getId());

        if (existingUser == null) {
            log.warn("Пользователь с id {} не найден для обновления", updateUser.getId());
            throw new NotFoundException("Пользователь с id " + updateUser.getId() + " не найден");
        }

        if (updateUser.getEmail() != null &&
                !existingUser.getEmail().equals(updateUser.getEmail())) {
            validateEmail(updateUser.getEmail(), updateUser.getId());

            emails.remove(existingUser.getEmail().toLowerCase());
            emails.add(updateUser.getEmail().toLowerCase());
        }

        if (updateUser.getName() != null) {
            existingUser.setName(updateUser.getName());
        }
        if (updateUser.getEmail() != null) {
            existingUser.setEmail(updateUser.getEmail());
        }

        log.info("Пользователь с id {} успешно обновлен", updateUser.getId());
        return existingUser;
    }

    @Override
    public List<User> findAll() {
        log.info("Список всех пользователей");
        return new ArrayList<>(users.values());
    }

    @Override
    public User findUserById(Long id) {
        log.info("Поиск пользователя по ID: {}", id);
        return users.get(id);
    }

    @Override
    public void delete(Long id) {
        log.info("Удаление пользователя по ID: {}", id);
        User user = users.get(id);
        if (user != null) {
            emails.remove(user.getEmail().toLowerCase());
        }
        users.remove(id);
    }

    private void validateEmail(String email, Long userId) throws DuplicatedDataException {
        if (email != null) {
            String emailLowerCase = email.toLowerCase();
            if (emails.contains(emailLowerCase)) {

                User existingUser = users.values().stream()
                        .filter(user -> user.getEmail().equalsIgnoreCase(emailLowerCase))
                        .findFirst()
                        .orElse(null);
                if (userId == null || (existingUser != null && !existingUser.getId().equals(userId))) {
                    log.error("Email {} уже используется", email);
                    throw new DuplicatedDataException("Этот email уже используется");
                }
            }
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L);
        return ++currentMaxId;
    }
}