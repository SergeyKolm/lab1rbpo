package org.example.service;

import org.example.model.*;
import org.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class TournamentService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private StandingRepository standingRepository;

    @Autowired
    private VenueRepository venueRepository;

    // 1. БИЗНЕС-ОПЕРАЦИЯ: Создание нового сезона
    @Transactional
    public void initializeNewSeason() {
        // Сбрасываем все матчи кроме FINISHED
        List<Match> matches = matchRepository.findByStatusNot("FINISHED");
        matchRepository.deleteAll(matches);

        // Сбрасываем статистику команд
        List<Team> teams = teamRepository.findAll();
        teams.forEach(team -> {
            team.setPoints(0);
            teamRepository.save(team);
        });

        // Сбрасываем турнирную таблицу
        standingRepository.deleteAll();

        // Создаем новые записи в таблице для каждой команды
        teams.forEach(team -> {
            Standing standing = new Standing();
            standing.setTeamId(team.getId());
            standing.setPosition(0);
            standingRepository.save(standing);
        });
    }

    // 2. БИЗНЕС-ОПЕРАЦИЯ: Получить статистику команды
    public Map<String, Object> getTeamStatistics(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new RuntimeException("Team not found");
        }

        // Все матчи команды
        List<Match> teamMatches = matchRepository.findByHomeTeamIdOrAwayTeamId(teamId, teamId);

        // Фильтруем завершенные матчи
        List<Match> finishedMatches = teamMatches.stream()
                .filter(m -> "FINISHED".equals(m.getStatus()))
                .collect(Collectors.toList());

        int wins = 0;
        int draws = 0;
        int losses = 0;
        int goalsFor = 0;
        int goalsAgainst = 0;

        for (Match match : finishedMatches) {
            boolean isHome = match.getHomeTeamId().equals(teamId);
            int teamGoals = isHome ? match.getHomeTeamScore() : match.getAwayTeamScore();
            int opponentGoals = isHome ? match.getAwayTeamScore() : match.getHomeTeamScore();

            goalsFor += teamGoals;
            goalsAgainst += opponentGoals;

            if (teamGoals > opponentGoals) {
                wins++;
            } else if (teamGoals < opponentGoals) {
                losses++;
            } else {
                draws++;
            }
        }

        // Игроки команды
        List<Player> teamPlayers = playerRepository.findByTeamId(teamId);
        Player topScorer = teamPlayers.stream()
                .max((p1, p2) -> Integer.compare(p1.getGoalsScored(), p2.getGoalsScored()))
                .orElse(null);

        return Map.of(
                "totalMatches", finishedMatches.size(),
                "wins", wins,
                "draws", draws,
                "losses", losses,
                "goalsFor", goalsFor,
                "goalsAgainst", goalsAgainst,
                "goalDifference", goalsFor - goalsAgainst,
                "winRate", finishedMatches.isEmpty() ? 0 : (double) wins / finishedMatches.size() * 100,
                "topScorer", topScorer != null ? Map.of(
                        "name", topScorer.getName(),
                        "goals", topScorer.getGoalsScored()
                ) : null,
                "squadSize", teamPlayers.size()
        );
    }

    // 3. БИЗНЕС-ОПЕРАЦИЯ: Назначить лучшего игрока матча
    @Transactional
    public Player assignManOfTheMatch(Long matchId, Long playerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (!"FINISHED".equals(match.getStatus())) {
            throw new RuntimeException("Match must be finished");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        // Проверяем, что игрок участвовал в матче
        Long playerTeamId = player.getTeamId();
        if (!playerTeamId.equals(match.getHomeTeamId()) &&
                !playerTeamId.equals(match.getAwayTeamId())) {
            throw new RuntimeException("Player did not participate in this match");
        }

        // Увеличиваем голы игрока (как пример награды)
        player.setGoalsScored(player.getGoalsScored() + 1);

        return playerRepository.save(player);
    }

    // 4. БИЗНЕС-ОПЕРАЦИЯ: Поиск свободных арен на дату
    public List<Venue> findAvailableVenues(LocalDateTime date) {
        // Все арены
        List<Venue> allVenues = venueRepository.findAll();

        // Арены, на которых есть матчи в эту дату
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<Match> matchesOnDate = matchRepository.findByMatchDateBetween(startOfDay, endOfDay);
        List<Long> busyVenueIds = matchesOnDate.stream()
                .map(Match::getVenueId)
                .distinct()
                .collect(Collectors.toList());

        // Фильтруем свободные арены
        return allVenues.stream()
                .filter(venue -> !busyVenueIds.contains(venue.getId()))
                .collect(Collectors.toList());
    }

    // 5. БИЗНЕС-ОПЕРАЦИЯ: Создать расписание для тура
    @Transactional
    public List<Match> generateRoundSchedule(List<Long> teamIds, LocalDateTime roundDate, Long venueId) {
        if (teamIds.size() % 2 != 0) {
            throw new RuntimeException("Number of teams must be even");
        }

        List<Match> createdMatches = new java.util.ArrayList<>();
        LocalDateTime matchTime = roundDate;

        // Простой алгоритм создания пар
        for (int i = 0; i < teamIds.size(); i += 2) {
            Match match = new Match();
            match.setHomeTeamId(teamIds.get(i));
            match.setAwayTeamId(teamIds.get(i + 1));
            match.setVenueId(venueId);
            match.setMatchDate(matchTime);
            match.setStatus("SCHEDULED");

            try {
                Match created = matchRepository.save(match);
                createdMatches.add(created);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create match: " + e.getMessage());
            }

            matchTime = matchTime.plusHours(3); // Следующий матч через 3 часа
        }

        return createdMatches;
    }
}