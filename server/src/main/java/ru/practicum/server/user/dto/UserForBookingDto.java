package ru.practicum.server.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.server.user.model.User;

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