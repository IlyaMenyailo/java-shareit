package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {

    Item create(Item item);

    Item update(Long itemId, Item updateItem, Long userId);

    Item findItemById(Long itemId);

    List<Item> findAll();

    List<Item> searchItems(String text);

    void deleteItem(Long itemId);
}
