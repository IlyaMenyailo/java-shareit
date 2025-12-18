package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.IllegalArgumentException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        if (itemDto.getRequestId() != null) {
            itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с ID " + itemDto.getRequestId() + " не найден."));
        }

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userId);
        Item createdItem = itemRepository.save(item);
        return ItemMapper.toItemDto(createdItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с ID " + itemId + " не найден."));

        if (!userId.equals(existingItem.getOwner())) {
            throw new NotFoundException("Только владелец может обновить вещь");
        }

        ItemMapper.updateItemFromDto(itemDto, existingItem);
        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemWithBookingsDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с ID " + itemId + " не найден."));

        ItemWithBookingsDto itemWithBookingsDto = ItemMapper.toItemWithBookingsDto(item);

        if (item.getOwner().equals(userId)) {
            Optional<Booking> lastBookingOpt = bookingRepository.findFirstByItemIdAndEndBeforeOrderByEndDesc(
                    itemId, LocalDateTime.now());
            Optional<Booking> nextBookingOpt = bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(
                    itemId, LocalDateTime.now());

            if (lastBookingOpt.isPresent()) {
                Booking lastBooking = lastBookingOpt.get();
                itemWithBookingsDto.setLastBooking(BookingShortDto.builder()
                        .id(lastBooking.getId())
                        .bookerId(lastBooking.getBooker().getId())
                        .start(lastBooking.getStart())
                        .end(lastBooking.getEnd())
                        .build());
            }

            if (nextBookingOpt.isPresent()) {
                Booking nextBooking = nextBookingOpt.get();
                itemWithBookingsDto.setNextBooking(BookingShortDto.builder()
                        .id(nextBooking.getId())
                        .bookerId(nextBooking.getBooker().getId())
                        .start(nextBooking.getStart())
                        .end(nextBooking.getEnd())
                        .build());
            }
        }

        List<Comment> comments = commentRepository.findByItemId(itemId);
        itemWithBookingsDto.setComments(comments.stream()
                .map(ItemMapper::toCommentDto)
                .collect(Collectors.toList()));

        return itemWithBookingsDto;
    }

    @Override
    public List<ItemWithBookingsDto> getItemsByOwner(Long ownerId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

        Page<Item> itemPage = itemRepository.findByOwnerOrderById(ownerId, pageable);
        List<Item> items = itemPage.getContent();

        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        Map<Long, List<Comment>> commentsByItem = commentRepository.findByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        return items.stream()
                .map(item -> {
                    ItemWithBookingsDto dto = ItemMapper.toItemWithBookingsDto(item);

                    Optional<Booking> lastBookingOpt = bookingRepository.findFirstByItemIdAndEndBeforeOrderByEndDesc(
                            item.getId(), LocalDateTime.now());
                    Optional<Booking> nextBookingOpt = bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(
                            item.getId(), LocalDateTime.now());

                    if (lastBookingOpt.isPresent()) {
                        Booking lastBooking = lastBookingOpt.get();
                        dto.setLastBooking(BookingShortDto.builder()
                                .id(lastBooking.getId())
                                .bookerId(lastBooking.getBooker().getId())
                                .start(lastBooking.getStart())
                                .end(lastBooking.getEnd())
                                .build());
                    }

                    if (nextBookingOpt.isPresent()) {
                        Booking nextBooking = nextBookingOpt.get();
                        dto.setNextBooking(BookingShortDto.builder()
                                .id(nextBooking.getId())
                                .bookerId(nextBooking.getBooker().getId())
                                .start(nextBooking.getStart())
                                .end(nextBooking.getEnd())
                                .build());
                    }

                    dto.setComments(commentsByItem.getOrDefault(item.getId(), Collections.emptyList())
                            .stream()
                            .map(ItemMapper::toCommentDto)
                            .collect(Collectors.toList()));

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text, Integer from, Integer size) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        Pageable pageable = PageRequest.of(from / size, size);

        return itemRepository.search(text, pageable).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, Long userId, CreateCommentDto commentDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с ID " + itemId + " не найден."));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        List<Booking> pastBookings = bookingRepository.findPastBookingsByItemIdAndBookerId(itemId, userId);
        if (pastBookings.isEmpty()) {
            throw new IllegalArgumentException("Пользователь не брал эту вещь в аренду или аренда еще не завершена.");
        }

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);
        return ItemMapper.toCommentDto(savedComment);
    }
}