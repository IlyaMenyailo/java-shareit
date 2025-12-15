package ru.practicum.server.request.service;

import ru.practicum.server.request.dto.ItemRequestDto;
import ru.practicum.server.request.dto.ItemRequestShortDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createItemRequest(Long userId, ItemRequestShortDto itemRequestShortDto);

    List<ItemRequestDto> getUserItemRequests(Long userId);

    List<ItemRequestDto> getAllItemRequests(Long userId, Integer from, Integer size);

    ItemRequestDto getItemRequestById(Long userId, Long requestId);
}