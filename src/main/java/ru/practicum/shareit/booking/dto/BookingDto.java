package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingDto {
    private Long id;

    @NotNull(message = "Дата начала не может быть пустой")
    @FutureOrPresent(message = "Дата начала должна быть в настоящем или будущем")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания не может быть пустой")
    @Future(message = "Дата окончания должна быть в будущем")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime end;

    @NotNull(message = "ID вещи не может быть пустым")
    private Long itemId;

    private BookingStatus status;
}