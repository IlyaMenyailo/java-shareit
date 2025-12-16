package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.util.HttpHeaders;

import java.util.Collections;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@Valid @RequestBody ItemDto itemDto,
                                             @RequestHeader(HttpHeaders.USER_ID_HEADER) Long ownerId) {
        log.info("Creating item for ownerId: {}", ownerId);
        return itemClient.createItem(itemDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@PathVariable Long itemId,
                                             @RequestBody ItemDto itemDto,
                                             @RequestHeader(HttpHeaders.USER_ID_HEADER) Long ownerId) {
        log.info("Updating item id: {} for ownerId: {}", itemId, ownerId);
        return itemClient.updateItem(itemId, itemDto, ownerId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable Long itemId,
                                              @RequestHeader(HttpHeaders.USER_ID_HEADER) Long userId) {
        log.info("Getting item by id: {} for user: {}", itemId, userId);
        return itemClient.getItemById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByOwner(@RequestHeader(HttpHeaders.USER_ID_HEADER) Long ownerId,
                                                  @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                  @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Getting items for ownerId: {}, from: {}, size: {}", ownerId, from, size);
        return itemClient.getItemsByOwner(ownerId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text,
                                              @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                              @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Searching items with text: '{}', from: {}, size: {}", text, from, size);
        if (text == null || text.isBlank()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return itemClient.searchItems(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@PathVariable Long itemId,
                                             @RequestHeader(HttpHeaders.USER_ID_HEADER) Long userId,
                                             @Valid @RequestBody CreateCommentDto commentDto) {
        log.info("Adding comment to item id: {} from user: {}", itemId, userId);
        return itemClient.addComment(itemId, userId, commentDto);
    }
}