package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.util.HttpHeaders;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createItemRequest(@Valid @RequestBody ItemRequestShortDto itemRequestShortDto,
                                                    @RequestHeader(HttpHeaders.USER_ID_HEADER) Long userId) {
        log.info("Creating item request by user: {}", userId);
        return itemRequestClient.createItemRequest(itemRequestShortDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserItemRequests(@RequestHeader(HttpHeaders.USER_ID_HEADER) Long userId) {
        log.info("Getting item requests for user: {}", userId);
        return itemRequestClient.getUserItemRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequests(@RequestHeader(HttpHeaders.USER_ID_HEADER) Long userId,
                                                     @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                     @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Getting all item requests for user: {}, from: {}, size: {}", userId, from, size);
        return itemRequestClient.getAllItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(@PathVariable Long requestId,
                                                     @RequestHeader(HttpHeaders.USER_ID_HEADER) Long userId) {
        log.info("Getting item request by id: {} for user: {}", requestId, userId);
        return itemRequestClient.getItemRequestById(requestId, userId);
    }
}