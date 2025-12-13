package org.example.service;

import org.example.model.Match;
import org.example.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamService teamService;

    @Autowired
    private StandingService standingService;

    @Autowired
    private VenueService venueService;

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public Optional<Match> getMatchById(Long id) {
        return matchRepository.findById(id);
    }

    public List<Match> getMatchesByTeam(Long teamId) {
        return matchRepository.findByHomeTeamIdOrAwayTeamId(teamId, teamId);
    }

    public List<Match> getUpcomingMatches() {
        return matchRepository.findByMatchDateAfter(LocalDateTime.now());
    }

    public List<Match> getFinishedMatches() {
        return matchRepository.findByMatchDateBefore(LocalDateTime.now());
    }

    public List<Match> getMatchesByStatus(String status) {
        return matchRepository.findByStatus(status);
    }

    public List<Match> getMatchesByVenue(Long venueId) {
        return matchRepository.findByVenueId(venueId);
    }

    @Transactional
    public Match createMatch(Match match) {
        // Проверка существования команд
        if (!teamService.existsById(match.getHomeTeamId())) {
            throw new RuntimeException("Home team not found with ID: " + match.getHomeTeamId());
        }
        if (!teamService.existsById(match.getAwayTeamId())) {
            throw new RuntimeException("Away team not found with ID: " + match.getAwayTeamId());
        }

        // Проверка: команда не может играть сама с собой
        if (match.getHomeTeamId().equals(match.getAwayTeamId())) {
            throw new RuntimeException("Team cannot play against itself");
        }

        // Проверка арены (если указана)
        if (match.getVenueId() != null && !venueService.existsById(match.getVenueId())) {
            throw new RuntimeException("Venue not found with ID: " + match.getVenueId());
        }

        // Проверка конфликта расписания
        LocalDateTime matchStart = match.getMatchDate();
        LocalDateTime matchEnd = matchStart.plusHours(2);

        List<Match> conflicts = matchRepository.findConflictingMatches(
                match.getHomeTeamId(), matchStart, matchEnd);
        conflicts.addAll(matchRepository.findConflictingMatches(
                match.getAwayTeamId(), matchStart, matchEnd));

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Team has scheduling conflict");
        }

        // Установка статуса по умолчанию
        if (match.getStatus() == null) {
            match.setStatus("SCHEDULED");
        }

        // Сброс счета для нового матча
        match.setHomeTeamScore(null);
        match.setAwayTeamScore(null);

        return matchRepository.save(match);
    }

    @Transactional
    public Match updateMatch(Long id, Match matchDetails) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found with ID: " + id));

        // Сохраняем старый статус для проверки перехода
        String oldStatus = match.getStatus();

        // ОБНОВЛЕНИЕ СЧЁТА
        if (matchDetails.getHomeTeamScore() != null) {
            match.setHomeTeamScore(matchDetails.getHomeTeamScore());
        }
        if (matchDetails.getAwayTeamScore() != null) {
            match.setAwayTeamScore(matchDetails.getAwayTeamScore());
        }

        // ОБНОВЛЕНИЕ СТАТУСА
        if (matchDetails.getStatus() != null) {
            String newStatus = matchDetails.getStatus();
            match.setStatus(newStatus);

            // Если матч переходит в статус FINISHED и есть счет, обновляем турнирную таблицу
            if ("FINISHED".equals(newStatus) && !"FINISHED".equals(oldStatus)) {
                if (match.getHomeTeamScore() != null && match.getAwayTeamScore() != null) {
                    standingService.updateStandingsAfterMatch(
                            match.getHomeTeamId(),
                            match.getAwayTeamId(),
                            match.getHomeTeamScore(),
                            match.getAwayTeamScore()
                    );
                } else {
                    throw new RuntimeException("Cannot finish match without score");
                }
            }
        }

        // Обновление остальных полей (оставить как есть)
        if (matchDetails.getHomeTeamId() != null) {
            if (!teamService.existsById(matchDetails.getHomeTeamId())) {
                throw new RuntimeException("Home team not found");
            }
            match.setHomeTeamId(matchDetails.getHomeTeamId());
        }
        if (matchDetails.getAwayTeamId() != null) {
            if (!teamService.existsById(matchDetails.getAwayTeamId())) {
                throw new RuntimeException("Away team not found");
            }
            match.setAwayTeamId(matchDetails.getAwayTeamId());
        }
        if (matchDetails.getVenueId() != null) {
            if (!venueService.existsById(matchDetails.getVenueId())) {
                throw new RuntimeException("Venue not found");
            }
            match.setVenueId(matchDetails.getVenueId());
        }
        if (matchDetails.getMatchDate() != null) {
            match.setMatchDate(matchDetails.getMatchDate());
        }

        return matchRepository.save(match);
    }

    @Transactional
    public void deleteMatch(Long id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found with ID: " + id));

        if ("IN_PROGRESS".equals(match.getStatus())) {
            throw new RuntimeException("Cannot delete match in progress");
        }

        matchRepository.deleteById(id);
    }

    // Бизнес-операция: Начать матч
    @Transactional
    public Match startMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found with ID: " + matchId));

        if (!"SCHEDULED".equals(match.getStatus())) {
            throw new RuntimeException("Match cannot be started. Current status: " + match.getStatus());
        }

        if (match.getMatchDate().isAfter(LocalDateTime.now().plusHours(1))) {
            throw new RuntimeException("Match cannot start more than 1 hour before scheduled time");
        }

        match.setStatus("IN_PROGRESS");
        return matchRepository.save(match);
    }

    // Бизнес-операция: Завершить матч
    @Transactional
    public Match completeMatch(Long matchId, Integer homeScore, Integer awayScore) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found with ID: " + matchId));

        if (!"IN_PROGRESS".equals(match.getStatus())) {
            throw new RuntimeException("Match is not in progress. Current status: " + match.getStatus());
        }

        if (homeScore == null || awayScore == null || homeScore < 0 || awayScore < 0) {
            throw new RuntimeException("Invalid score");
        }

        match.setHomeTeamScore(homeScore);
        match.setAwayTeamScore(awayScore);
        match.setStatus("FINISHED");

        // Обновляем турнирную таблицу
        standingService.updateStandingsAfterMatch(
                match.getHomeTeamId(),
                match.getAwayTeamId(),
                homeScore,
                awayScore
        );

        return matchRepository.save(match);
    }

    // Бизнес-операция: Отменить матч
    @Transactional
    public Match cancelMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found with ID: " + matchId));

        if ("FINISHED".equals(match.getStatus())) {
            throw new RuntimeException("Cannot cancel finished match");
        }

        match.setStatus("CANCELLED");
        return matchRepository.save(match);
    }

    // Бизнес-операция: Получить статистику матчей команды
    public Map<String, Object> getTeamMatchStatistics(Long teamId) {
        List<Match> allMatches = getMatchesByTeam(teamId);
        List<Match> finishedMatches = allMatches.stream()
                .filter(m -> "FINISHED".equals(m.getStatus()))
                .collect(Collectors.toList());

        int totalMatches = finishedMatches.size();
        int homeMatches = 0;
        int awayMatches = 0;
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

            if (isHome) homeMatches++;
            else awayMatches++;

            if (teamGoals > opponentGoals) wins++;
            else if (teamGoals < opponentGoals) losses++;
            else draws++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalMatches", totalMatches);
        result.put("homeMatches", homeMatches);
        result.put("awayMatches", awayMatches);
        result.put("wins", wins);
        result.put("draws", draws);
        result.put("losses", losses);
        result.put("goalsFor", goalsFor);
        result.put("goalsAgainst", goalsAgainst);
        result.put("goalDifference", goalsFor - goalsAgainst);
        result.put("winRate", totalMatches > 0 ? (double) wins / totalMatches * 100 : 0);
        result.put("drawRate", totalMatches > 0 ? (double) draws / totalMatches * 100 : 0);
        result.put("lossRate", totalMatches > 0 ? (double) losses / totalMatches * 100 : 0);

        return result;
    }
}