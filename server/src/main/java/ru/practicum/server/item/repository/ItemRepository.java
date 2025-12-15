package ru.practicum.server.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.server.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerOrderById(Long ownerId);

    @Query("SELECT i FROM Item i " +
            "WHERE i.available = true " +
            "AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')))")
    List<Item> search(String text);

    List<Item> findByRequestIn(List<Long> requestIds);

    List<Item> findByRequest(Long requestId);

    @Query("SELECT i FROM Item i WHERE i.request IN :requestIds")
    List<Item> findItemsByRequestIds(List<Long> requestIds);
}