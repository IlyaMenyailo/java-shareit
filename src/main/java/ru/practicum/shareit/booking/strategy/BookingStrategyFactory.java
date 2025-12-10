package ru.practicum.shareit.booking.strategy;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.strategy.owner.*;
import ru.practicum.shareit.booking.strategy.state.*;

@Component
public class BookingStrategyFactory {

    public BookingStateStrategy getUserStrategy(BookingState state, Pageable pageable) {
        switch (state) {
            case ALL:
                return new AllBookingStateStrategy(pageable);
            case CURRENT:
                return new CurrentBookingStateStrategy(pageable);
            case PAST:
                return new PastBookingStateStrategy(pageable);
            case FUTURE:
                return new FutureBookingStateStrategy(pageable);
            case WAITING:
                return new WaitingBookingStateStrategy(pageable);
            case REJECTED:
                return new RejectedBookingStateStrategy(pageable);
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    public BookingStateStrategy getOwnerStrategy(BookingState state, Pageable pageable) {
        switch (state) {
            case ALL:
                return new OwnerAllBookingStateStrategy(pageable);
            case CURRENT:
                return new OwnerCurrentBookingStateStrategy(pageable);
            case PAST:
                return new OwnerPastBookingStateStrategy(pageable);
            case FUTURE:
                return new OwnerFutureBookingStateStrategy(pageable);
            case WAITING:
                return new OwnerWaitingBookingStateStrategy(pageable);
            case REJECTED:
                return new OwnerRejectedBookingStateStrategy(pageable);
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
    }
}