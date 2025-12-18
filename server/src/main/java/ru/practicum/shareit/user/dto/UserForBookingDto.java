package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserForBookingDto {
    private Long id;

    public static UserForBookingDto toDto(User user) {
        return UserForBookingDto.builder()
                .id(user.getId())
                .build();
    }
}