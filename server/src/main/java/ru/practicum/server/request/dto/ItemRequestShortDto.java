package ru.practicum.server.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemRequestShortDto {
    @NotBlank(message = "Описание запроса не может быть пустым")
    private String description;
}