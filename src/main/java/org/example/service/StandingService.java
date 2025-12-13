package org.example.service;

import org.example.model.Standing;
import org.example.repository.StandingRepository;
import org.example.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class StandingService {

    @Autowired
    private StandingRepository standingRepository;

    @Autowired
    private TeamRepository teamRepository;

    public List<Standing> getAllStandings() {
        return standingRepository.findAllByOrderByPointsDescGoalDifferenceDescGoalsForDesc();
    }

    public List<Standing> getTopStandings(Integer limit) {
        List<Standing> allStandings = getAllStandings();
        return limit != null && limit < allStandings.size() ?
                allStandings.subList(0, limit) : allStandings;
    }

    public Optional<Standing> getStandingById(Long id) {
        return standingRepository.findById(id);
    }

    public Optional<Standing> getStandingByTeamId(Long teamId) {
        return standingRepository.findByTeamId(teamId);
    }

    public Integer getPositionByTeamId(Long teamId) {
        return standingRepository.getPositionByTeamId(teamId);
    }

    @Transactional
    public Standing createStanding(Standing standing) {
        // Проверка существования команды
        if (!teamRepository.existsById(standing.getTeamId())) {
            throw new RuntimeException("Team not found with ID: " + standing.getTeamId());
        }

        // Проверка уникальности teamId
        if (standingRepository.findByTeamId(standing.getTeamId()).isPresent()) {
            throw new RuntimeException("Standing already exists for team ID: " + standing.getTeamId());
        }

        // Установка значений по умолчанию
        if (standing.getMatchesPlayed() == null) standing.setMatchesPlayed(0);
        if (standing.getWins() == null) standing.setWins(0);
        if (standing.getDraws() == null) standing.setDraws(0);
        if (standing.getLosses() == null) standing.setLosses(0);
        if (standing.getGoalsFor() == null) standing.setGoalsFor(0);
        if (standing.getGoalsAgainst() == null) standing.setGoalsAgainst(0);
        if (standing.getGoalDifference() == null) standing.setGoalDifference(0);
        if (standing.getPoints() == null) standing.setPoints(0);

        Standing savedStanding = standingRepository.save(standing);

        // Обновляем позицию
        updatePositions();

        return savedStanding;
    }

    @Transactional
    public void createStandingForTeam(Long teamId) {
        if (standingRepository.findByTeamId(teamId).isPresent()) {
            return; // Уже существует
        }

        Standing standing = new Standing();
        standing.setTeamId(teamId);
        standing.setPosition(0);
        standing.setMatchesPlayed(0);
        standing.setWins(0);
        standing.setDraws(0);
        standing.setLosses(0);
        standing.setGoalsFor(0);
        standing.setGoalsAgainst(0);
        standing.setGoalDifference(0);
        standing.setPoints(0);

        standingRepository.save(standing);
    }

    @Transactional
    public Standing updateStanding(Long id, Standing standingDetails) {
        Standing standing = standingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Standing not found with ID: " + id));

        // Обновляем только разрешенные поля
        if (standingDetails.getMatchesPlayed() != null) {
            standing.setMatchesPlayed(standingDetails.getMatchesPlayed());
        }
        if (standingDetails.getWins() != null) {
            standing.setWins(standingDetails.getWins());
        }
        if (standingDetails.getDraws() != null) {
            standing.setDraws(standingDetails.getDraws());
        }
        if (standingDetails.getLosses() != null) {
            standing.setLosses(standingDetails.getLosses());
        }
        if (standingDetails.getGoalsFor() != null) {
            standing.setGoalsFor(standingDetails.getGoalsFor());
            standing.setGoalDifference(standing.getGoalsFor() - standing.getGoalsAgainst());
        }
        if (standingDetails.getGoalsAgainst() != null) {
            standing.setGoalsAgainst(standingDetails.getGoalsAgainst());
            standing.setGoalDifference(standing.getGoalsFor() - standing.getGoalsAgainst());
        }
        if (standingDetails.getPoints() != null) {
            standing.setPoints(standingDetails.getPoints());
        }

        Standing updated = standingRepository.save(standing);

        // Обновляем позиции
        updatePositions();

        return updated;
    }

    @Transactional
    public void updateStandingsAfterMatch(Long homeTeamId, Long awayTeamId,
                                          Integer homeScore, Integer awayScore) {
        Standing homeStanding = standingRepository.findByTeamId(homeTeamId)
                .orElseThrow(() -> new RuntimeException("Standing not found for home team ID: " + homeTeamId));

        Standing awayStanding = standingRepository.findByTeamId(awayTeamId)
                .orElseThrow(() -> new RuntimeException("Standing not found for away team ID: " + awayTeamId));

        // Обновляем матчи
        homeStanding.setMatchesPlayed(homeStanding.getMatchesPlayed() + 1);
        awayStanding.setMatchesPlayed(awayStanding.getMatchesPlayed() + 1);

        // Обновляем голы
        homeStanding.setGoalsFor(homeStanding.getGoalsFor() + homeScore);
        homeStanding.setGoalsAgainst(homeStanding.getGoalsAgainst() + awayScore);
        homeStanding.setGoalDifference(homeStanding.getGoalsFor() - homeStanding.getGoalsAgainst());

        awayStanding.setGoalsFor(awayStanding.getGoalsFor() + awayScore);
        awayStanding.setGoalsAgainst(awayStanding.getGoalsAgainst() + homeScore);
        awayStanding.setGoalDifference(awayStanding.getGoalsFor() - awayStanding.getGoalsAgainst());

        // Определяем результат
        if (homeScore > awayScore) {
            // Победа хозяев
            homeStanding.setWins(homeStanding.getWins() + 1);
            homeStanding.setPoints(homeStanding.getPoints() + 3);
            awayStanding.setLosses(awayStanding.getLosses() + 1);
        } else if (homeScore < awayScore) {
            // Победа гостей
            awayStanding.setWins(awayStanding.getWins() + 1);
            awayStanding.setPoints(awayStanding.getPoints() + 3);
            homeStanding.setLosses(homeStanding.getLosses() + 1);
        } else {
            // Ничья
            homeStanding.setDraws(homeStanding.getDraws() + 1);
            homeStanding.setPoints(homeStanding.getPoints() + 1);
            awayStanding.setDraws(awayStanding.getDraws() + 1);
            awayStanding.setPoints(awayStanding.getPoints() + 1);
        }

        standingRepository.save(homeStanding);
        standingRepository.save(awayStanding);

        // Обновляем позиции
        updatePositions();

        // Обновляем очки в таблице Team
        teamRepository.findById(homeTeamId).ifPresent(team -> {
            team.setPoints(homeStanding.getPoints());
            teamRepository.save(team);
        });

        teamRepository.findById(awayTeamId).ifPresent(team -> {
            team.setPoints(awayStanding.getPoints());
            teamRepository.save(team);
        });
    }

    @Transactional
    public void deleteStanding(Long id) {
        if (!standingRepository.existsById(id)) {
            throw new RuntimeException("Standing not found with ID: " + id);
        }

        standingRepository.deleteById(id);
    }

    @Transactional
    public void deleteByTeamId(Long teamId) {
        standingRepository.findByTeamId(teamId).ifPresent(standing -> {
            standingRepository.delete(standing);
        });
    }

    // Бизнес-операция: Обновить все позиции
    @Transactional
    public void updatePositions() {
        List<Standing> standings = standingRepository.findAllByOrderByPointsDescGoalDifferenceDescGoalsForDesc();

        for (int i = 0; i < standings.size(); i++) {
            Standing standing = standings.get(i);
            standing.setPosition(i + 1);
            standingRepository.save(standing);
        }
    }

    // Бизнес-операция: Сбросить все статистики
    @Transactional
    public void resetAllStandings() {
        List<Standing> allStandings = standingRepository.findAll();
        allStandings.forEach(standing -> {
            standing.setMatchesPlayed(0);
            standing.setWins(0);
            standing.setDraws(0);
            standing.setLosses(0);
            standing.setGoalsFor(0);
            standing.setGoalsAgainst(0);
            standing.setGoalDifference(0);
            standing.setPoints(0);
            standing.setPosition(0);
            standingRepository.save(standing);
        });
    }

    // Бизнес-операция: Получить статистику лиги
    public Map<String, Object> getLeagueStats() {
        List<Standing> allStandings = getAllStandings();

        if (allStandings.isEmpty()) {
            return Map.of(
                    "totalTeams", 0,
                    "totalMatches", 0,
                    "totalGoals", 0,
                    "averageGoalsPerMatch", 0.0
            );
        }

        int totalMatches = allStandings.stream()
                .mapToInt(Standing::getMatchesPlayed)
                .sum() / 2; // Каждый матч учтен дважды

        int totalGoals = allStandings.stream()
                .mapToInt(Standing::getGoalsFor)
                .sum();

        double averageGoalsPerMatch = totalMatches > 0 ?
                (double) totalGoals / totalMatches : 0;

        // Команда с лучшей атакой и защитой
        Optional<Standing> bestAttack = allStandings.stream()
                .max((s1, s2) -> Integer.compare(s1.getGoalsFor(), s2.getGoalsFor()));

        Optional<Standing> bestDefense = allStandings.stream()
                .min((s1, s2) -> Integer.compare(s1.getGoalsAgainst(), s2.getGoalsAgainst()));

        // Команды в зонах еврокубков и вылета
        List<String> championsLeagueTeams = allStandings.stream()
                .filter(s -> s.getPosition() <= 4)
                .map(s -> s.getPosition() + ". Team ID: " + s.getTeamId())
                .collect(Collectors.toList());

        List<String> relegationZoneTeams = allStandings.stream()
                .filter(s -> s.getPosition() > allStandings.size() - 3)
                .map(s -> s.getPosition() + ". Team ID: " + s.getTeamId())
                .collect(Collectors.toList());

        return Map.of(
                "totalTeams", allStandings.size(),
                "totalMatches", totalMatches,
                "totalGoals", totalGoals,
                "averageGoalsPerMatch", Math.round(averageGoalsPerMatch * 100.0) / 100.0,
                "bestAttack", bestAttack.map(s -> "Team ID: " + s.getTeamId() + " (" + s.getGoalsFor() + " goals)").orElse("None"),
                "bestDefense", bestDefense.map(s -> "Team ID: " + s.getTeamId() + " (" + s.getGoalsAgainst() + " goals conceded)").orElse("None"),
                "championsLeagueZone", championsLeagueTeams,
                "relegationZone", relegationZoneTeams
        );
    }

    // Бизнес-операция: Прогноз чемпиона
    public Map<String, Object> predictChampion() {
        List<Standing> standings = getTopStandings(3);

        if (standings.isEmpty()) {
            return Map.of("prediction", "Not enough data");
        }

        Standing leader = standings.get(0);
        double winProbability = 75.0; // Простой пример

        Map<String, Double> probabilities = new HashMap<>();
        for (Standing standing : standings) {
            probabilities.put("Team " + standing.getTeamId(),
                    standing.equals(leader) ? winProbability : (25.0 / (standings.size() - 1)));
        }

        return Map.of(
                "currentLeader", "Team ID: " + leader.getTeamId(),
                "points", leader.getPoints(),
                "winProbability", winProbability,
                "top3Probabilities", probabilities
        );
    }
}