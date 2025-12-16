package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerOrderById(Long ownerId);

    Page<Item> findByOwnerOrderById(Long ownerId, Pageable pageable);

    @Query("SELECT i FROM Item i " +
            "WHERE i.available = true " +
            "AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')))")
    List<Item> search(@Param("text") String text);

    @Query("SELECT i FROM Item i " +
            "WHERE i.available = true " +
            "AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')))")
    Page<Item> search(@Param("text") String text, Pageable pageable);

    List<Item> findByRequestIn(List<Long> requestIds);

    List<Item> findByRequest(Long requestId);

    @Query("SELECT i FROM Item i WHERE i.request IN :requestIds")
    List<Item> findItemsByRequestIds(@Param("requestIds") List<Long> requestIds);
}