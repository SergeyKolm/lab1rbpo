package org.example.repository;

import org.example.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByName(String name);

    boolean existsByName(String name);

    // Поиск по городу
    List<Team> findByCity(String city);

    // Команды с определенным количеством очков или больше
    List<Team> findByPointsGreaterThanEqual(Integer points);

    // Топ команд по очкам
    List<Team> findAllByOrderByPointsDesc();

    // Поиск по году основания
    List<Team> findByFoundationYear(Integer year);

    // Поиск по диапазону годов
    List<Team> findByFoundationYearBetween(Integer startYear, Integer endYear);

    // Команды с определенным тренером
    List<Team> findByCoachName(String coachName);

    // Количество команд в городе
    @Query("SELECT COUNT(t) FROM Team t WHERE t.city = :city")
    Long countByCity(@Param("city") String city);
}