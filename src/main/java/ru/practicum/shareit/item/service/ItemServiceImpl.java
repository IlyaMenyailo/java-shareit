package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {

        if (userRepository.findUserById(userId) == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userId);
        Item createdItem = itemRepository.create(item);
        return ItemMapper.toItemDto(createdItem);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        Item existingItem = itemRepository.findItemById(itemId);

        if (!userId.equals(existingItem.getOwner())) {
            throw new NotFoundException("Только пользователь может обновить вещь");
        }

        ItemMapper.updateItemFromDto(itemDto, existingItem);

        Item updatedItem = itemRepository.update(existingItem.getId(), existingItem, userId);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = itemRepository.findItemById(itemId);
        if (item == null) {
            throw new NotFoundException("Предмет с ID " + itemId + " не найден.");
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {
        return itemRepository.findAll().stream()
                .filter(item -> Objects.equals(item.getOwner(), userId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        return itemRepository.searchItems(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItem(Long itemId) {
        itemRepository.deleteItem(itemId);
    }
}