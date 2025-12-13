package org.example.repository;

import org.example.model.Standing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StandingRepository extends JpaRepository<Standing, Long> {

    Optional<Standing> findByTeamId(Long teamId);

    // Получить всю таблицу отсортированную
    List<Standing> findAllByOrderByPointsDescGoalDifferenceDescGoalsForDesc();

    // Топ N команд
    List<Standing> findTop5ByOrderByPointsDescGoalDifferenceDescGoalsForDesc();

    // Команды с положительной разницей голов
    List<Standing> findByGoalDifferenceGreaterThan(Integer difference);

    // Команды без побед
    List<Standing> findByWins(Integer wins);

    // Команды с определенным количеством очков или больше
    List<Standing> findByPointsGreaterThanEqual(Integer points);

    // Обновление позиций
    @Query(value = "UPDATE standings SET position = :position WHERE id = :id", nativeQuery = true)
    void updatePosition(@Param("id") Long id, @Param("position") Integer position);

    // Позиция команды в таблице (1-based)
    @Query(value = """
        SELECT COUNT(*) + 1 FROM standings s 
        WHERE s.points > (SELECT points FROM standings WHERE team_id = :teamId) 
        OR (s.points = (SELECT points FROM standings WHERE team_id = :teamId) 
        AND s.goal_difference > (SELECT goal_difference FROM standings WHERE team_id = :teamId))
        OR (s.points = (SELECT points FROM standings WHERE team_id = :teamId) 
        AND s.goal_difference = (SELECT goal_difference FROM standings WHERE team_id = :teamId)
        AND s.goals_for > (SELECT goals_for FROM standings WHERE team_id = :teamId))
        """, nativeQuery = true)
    Integer getPositionByTeamId(@Param("teamId") Long teamId);

    // Общее количество забитых голов в чемпионате
    @Query("SELECT SUM(s.goalsFor) FROM Standing s")
    Integer getTotalGoalsInLeague();

    // Среднее количество очков
    @Query("SELECT AVG(s.points) FROM Standing s")
    Double getAveragePoints();
}