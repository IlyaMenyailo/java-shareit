package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ItemRepositoryImpl implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Item create(Item item) {
        item.setId(getNextId());
        items.put(item.getId(), item);
        log.info("Создан предмет с ID: {}", item.getId());
        return item;
    }

    @Override
    public Item update(Long itemId, Item updateItem, Long userId) {
        Item oldItem = items.get(itemId);

        if (oldItem == null) {
            throw new NotFoundException("Предмет с ID " + itemId + " не найден.");
        }

        if (!Objects.equals(oldItem.getOwner(), userId)) {
            throw new SecurityException("У вас нет прав на обновление этого предмета.");
        }

        Optional.ofNullable(updateItem.getName()).ifPresent(oldItem::setName);
        Optional.ofNullable(updateItem.getDescription()).ifPresent(oldItem::setDescription);
        Optional.ofNullable(updateItem.getAvailable()).ifPresent(oldItem::setAvailable);

        items.put(itemId, oldItem);
        log.info("Обновлен предмет с ID: {}", itemId);
        return oldItem;
    }

    @Override
    public Item findItemById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NotFoundException("Предмет с ID " + itemId + " не найден.");
        }
        log.info("Получен предмет с ID: {}", itemId);
        return item;
    }

    @Override
    public Collection<Item> findAll() {
        log.info("Список всех пользователей");
        return items.values();
    }

    @Override
    public List<Item> searchItems(String text) {

        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        String searchText = text.toLowerCase();
        List<Item> searchResults = items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(searchText) ||
                        item.getDescription().toLowerCase().contains(searchText))
                .collect(Collectors.toList());

        log.info("Поиск предметов по тексту: {}", text);
        return searchResults;
    }

    @Override
    public void deleteItem(Long itemId) {
        log.info("Удален предмет с ID: {}", itemId);
        items.remove(itemId);
    }

    private long getNextId() {
        long currentMaxId = items.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}