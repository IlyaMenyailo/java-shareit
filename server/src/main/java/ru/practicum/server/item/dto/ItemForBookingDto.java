package ru.practicum.server.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.server.item.model.Item;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemForBookingDto {
    private Long id;
    private String name;

    public static ItemForBookingDto toDto(Item item) {
        return ItemForBookingDto.builder()
                .id(item.getId())
                .name(item.getName())
                .build();
    }
}