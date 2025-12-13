package org.example.repository;

import org.example.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    // Поиск матчей по статусу
    List<Match> findByStatus(String status);

    // Поиск матчей по статусу (НЕ равному указанному)
    List<Match> findByStatusNot(String status);

    // Поиск матчей команды (домашние или гостевые)
    List<Match> findByHomeTeamIdOrAwayTeamId(Long homeTeamId, Long awayTeamId);

    // Поиск матчей по дате
    List<Match> findByMatchDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Матчи после указанной даты (предстоящие)
    List<Match> findByMatchDateAfter(LocalDateTime date);

    // Матчи до указанной даты (прошедшие)
    List<Match> findByMatchDateBefore(LocalDateTime date);

    // Матчи на конкретной арене
    List<Match> findByVenueId(Long venueId);

    // Проверка конфликта расписания
    @Query("SELECT m FROM Match m WHERE " +
            "(m.homeTeamId = :teamId OR m.awayTeamId = :teamId) AND " +
            "m.matchDate BETWEEN :startTime AND :endTime AND " +
            "m.status != 'FINISHED'")
    List<Match> findConflictingMatches(
            @Param("teamId") Long teamId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // Поиск матчей с определенным счетом
    @Query("SELECT m FROM Match m WHERE m.status = 'FINISHED' AND " +
            "(m.homeTeamScore > m.awayTeamScore OR m.awayTeamScore > m.homeTeamScore)")
    List<Match> findMatchesWithWinner();

    // Количество матчей команды
    @Query("SELECT COUNT(m) FROM Match m WHERE (m.homeTeamId = :teamId OR m.awayTeamId = :teamId) AND m.status = 'FINISHED'")
    Long countMatchesByTeamId(@Param("teamId") Long teamId);
}