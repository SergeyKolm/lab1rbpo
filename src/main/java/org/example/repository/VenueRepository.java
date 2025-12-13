package org.example.repository;

import org.example.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    Optional<Venue> findByName(String name);

    List<Venue> findByCity(String city);

    // Арены с вместимостью больше указанной
    List<Venue> findByCapacityGreaterThanEqual(Integer capacity);

    // Арены с вместимостью меньше указанной
    List<Venue> findByCapacityLessThanEqual(Integer capacity);

    // Арены по типу покрытия
    List<Venue> findByFieldType(String fieldType);

    // Арены в городе с определенной минимальной вместимостью
    @Query("SELECT v FROM Venue v WHERE v.city = :city AND v.capacity >= :minCapacity ORDER BY v.capacity DESC")
    List<Venue> findByCityAndMinCapacity(
            @Param("city") String city,
            @Param("minCapacity") Integer minCapacity);

    // Арены отсортированные по вместимости
    List<Venue> findAllByOrderByCapacityDesc();

    // Общая вместимость всех арен в городе
    @Query("SELECT SUM(v.capacity) FROM Venue v WHERE v.city = :city")
    Integer getTotalCapacityByCity(@Param("city") String city);

    // Поиск по части названия
    @Query("SELECT v FROM Venue v WHERE LOWER(v.name) LIKE LOWER(CONCAT('%', :namePart, '%'))")
    List<Venue> findByNameContainingIgnoreCase(@Param("namePart") String namePart);
}