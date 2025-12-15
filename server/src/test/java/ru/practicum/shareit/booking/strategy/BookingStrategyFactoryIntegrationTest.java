package ru.practicum.shareit.booking.strategy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.BookingState;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookingStrategyFactoryIntegrationTest {

    @Autowired
    private BookingStrategyFactory bookingStrategyFactory;

    @Test
    void contextLoads() {
        assertNotNull(bookingStrategyFactory);
    }

    @Test
    void getAllStrategiesShouldBeAvailable() {
        for (BookingState state : BookingState.values()) {
            assertDoesNotThrow(() -> {
                BookingStateStrategy userStrategy = bookingStrategyFactory.getUserStrategy(state);
                assertNotNull(userStrategy);
                assertEquals(state, userStrategy.getState());
                assertFalse(userStrategy.isForOwner());

                BookingStateStrategy ownerStrategy = bookingStrategyFactory.getOwnerStrategy(state);
                assertNotNull(ownerStrategy);
                assertEquals(state, ownerStrategy.getState());
                assertTrue(ownerStrategy.isForOwner());
            });
        }
    }

    @Test
    void strategiesShouldHaveCorrectTypes() {
        BookingStateStrategy userAllStrategy = bookingStrategyFactory.getUserStrategy(BookingState.ALL);
        assertEquals(BookingState.ALL, userAllStrategy.getState());
        assertFalse(userAllStrategy.isForOwner());

        BookingStateStrategy ownerAllStrategy = bookingStrategyFactory.getOwnerStrategy(BookingState.ALL);
        assertEquals(BookingState.ALL, ownerAllStrategy.getState());
        assertTrue(ownerAllStrategy.isForOwner());

        BookingStateStrategy userWaitingStrategy = bookingStrategyFactory.getUserStrategy(BookingState.WAITING);
        assertEquals(BookingState.WAITING, userWaitingStrategy.getState());
        assertFalse(userWaitingStrategy.isForOwner());

        BookingStateStrategy ownerWaitingStrategy = bookingStrategyFactory.getOwnerStrategy(BookingState.WAITING);
        assertEquals(BookingState.WAITING, ownerWaitingStrategy.getState());
        assertTrue(ownerWaitingStrategy.isForOwner());
    }
}