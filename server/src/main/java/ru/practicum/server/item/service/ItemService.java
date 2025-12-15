package ru.practicum.server.item.service;

import ru.practicum.server.item.dto.CommentDto;
import ru.practicum.server.item.dto.CreateCommentDto;
import ru.practicum.server.item.dto.ItemDto;
import ru.practicum.server.item.dto.ItemWithBookingsDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId);

    ItemWithBookingsDto getItemById(Long itemId, Long userId);

    List<ItemWithBookingsDto> getItemsByOwner(Long ownerId);

    List<ItemDto> searchItems(String text);

    void deleteItem(Long itemId);

    CommentDto addComment(Long itemId, Long userId, CreateCommentDto commentDto);
}