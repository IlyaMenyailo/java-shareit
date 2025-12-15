package ru.practicum.server.booking.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.server.booking.BookingState;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class BookingStrategyFactory {
    private final List<BookingStateStrategy> strategies;
    private final Map<String, BookingStateStrategy> strategyCache = new ConcurrentHashMap<>();

    public BookingStateStrategy getUserStrategy(BookingState state) {
        String key = createKey(state, false);
        return strategyCache.computeIfAbsent(key, k -> findStrategy(state, false));
    }

    public BookingStateStrategy getOwnerStrategy(BookingState state) {
        String key = createKey(state, true);
        return strategyCache.computeIfAbsent(key, k -> findStrategy(state, true));
    }

    private BookingStateStrategy findStrategy(BookingState state, boolean forOwner) {
        return strategies.stream()
                .filter(strategy -> strategy.getState() == state && strategy.isForOwner() == forOwner)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Strategy for state '%s' and owner=%s not found", state, forOwner)));
    }

    private String createKey(BookingState state, boolean forOwner) {
        return state.name() + "_" + (forOwner ? "OWNER" : "USER");
    }
}