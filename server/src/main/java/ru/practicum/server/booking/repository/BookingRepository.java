package ru.practicum.server.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.server.booking.model.Booking;
import ru.practicum.server.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.start < :currentTime AND b.end > :currentTime ORDER BY b.start DESC")
    List<Booking> findCurrentBookingsByBookerId(Long bookerId, LocalDateTime currentTime, Pageable pageable);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner = :ownerId ORDER BY b.start DESC")
    List<Booking> findByItemOwnerId(Long ownerId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner = :ownerId AND b.start < :currentTime AND b.end > :currentTime ORDER BY b.start DESC")
    List<Booking> findCurrentBookingsByItemOwnerId(Long ownerId, LocalDateTime currentTime, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner = :ownerId AND b.end < :end ORDER BY b.start DESC")
    List<Booking> findPastBookingsByItemOwnerId(Long ownerId, LocalDateTime end, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner = :ownerId AND b.start > :start ORDER BY b.start DESC")
    List<Booking> findFutureBookingsByItemOwnerId(Long ownerId, LocalDateTime start, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner = :ownerId AND b.status = :status ORDER BY b.start DESC")
    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.booker.id = :bookerId AND b.status = 'APPROVED' AND b.end < CURRENT_TIMESTAMP")
    List<Booking> findPastBookingsByItemIdAndBookerId(Long itemId, Long bookerId);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.end < :dateTime ORDER BY b.end DESC")
    List<Booking> findByItemIdAndEndBeforeOrderByEndDesc(Long itemId, LocalDateTime dateTime);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.start > :dateTime ORDER BY b.start ASC")
    List<Booking> findByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime dateTime);

    default Optional<Booking> findFirstByItemIdAndEndBeforeOrderByEndDesc(Long itemId, LocalDateTime dateTime) {
        List<Booking> bookings = findByItemIdAndEndBeforeOrderByEndDesc(itemId, dateTime);
        return bookings.isEmpty() ? Optional.empty() : Optional.of(bookings.get(0));
    }

    default Optional<Booking> findFirstByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime dateTime) {
        List<Booking> bookings = findByItemIdAndStartAfterOrderByStartAsc(itemId, dateTime);
        return bookings.isEmpty() ? Optional.empty() : Optional.of(bookings.get(0));
    }
}