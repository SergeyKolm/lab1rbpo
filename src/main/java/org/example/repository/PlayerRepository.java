package org.example.repository;

import org.example.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findByTeamId(Long teamId);

    List<Player> findByPosition(String position);

    List<Player> findByGoalsScoredGreaterThan(Integer goals);

    List<Player> findByAgeGreaterThanEqual(Integer age);

    List<Player> findByAgeLessThanEqual(Integer age);

    // Поиск по диапазону возрастов
    List<Player> findByAgeBetween(Integer minAge, Integer maxAge);

    // Поиск по номеру на футболке
    List<Player> findByJerseyNumber(Integer jerseyNumber);

    // Топ бомбардиры
    List<Player> findAllByOrderByGoalsScoredDesc();

    // Игроки команды по позициям
    @Query("SELECT p FROM Player p WHERE p.teamId = :teamId AND p.position = :position ORDER BY p.jerseyNumber")
    List<Player> findByTeamIdAndPosition(
            @Param("teamId") Long teamId,
            @Param("position") String position);

    // Средний возраст команды
    @Query("SELECT AVG(p.age) FROM Player p WHERE p.teamId = :teamId")
    Double getAverageAgeByTeamId(@Param("teamId") Long teamId);

    // Общее количество голов команды
    @Query("SELECT SUM(p.goalsScored) FROM Player p WHERE p.teamId = :teamId")
    Integer getTotalGoalsByTeamId(@Param("teamId") Long teamId);

    // Проверка уникальности номера в команде
    @Query("SELECT COUNT(p) > 0 FROM Player p WHERE p.teamId = :teamId AND p.jerseyNumber = :jerseyNumber")
    boolean existsByTeamIdAndJerseyNumber(
            @Param("teamId") Long teamId,
            @Param("jerseyNumber") Integer jerseyNumber);
}