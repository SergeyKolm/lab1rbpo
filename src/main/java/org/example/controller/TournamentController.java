package org.example.controller;

import org.example.model.Match;
import org.example.model.Player;
import org.example.model.Venue;
import org.example.service.TournamentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tournament")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    // 1. Инициализация нового сезона
    @PostMapping("/season/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> initializeNewSeason() {
        try {
            tournamentService.initializeNewSeason();
            return ResponseEntity.ok("New season initialized successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. Получение статистики команды
    @GetMapping("/team/{teamId}/statistics")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getTeamStatistics(@PathVariable Long teamId) {
        try {
            Map<String, Object> statistics = tournamentService.getTeamStatistics(teamId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. Назначение лучшего игрока матча
    @PostMapping("/match/{matchId}/man-of-the-match/{playerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignManOfTheMatch(
            @PathVariable Long matchId,
            @PathVariable Long playerId) {
        try {
            Player player = tournamentService.assignManOfTheMatch(matchId, playerId);
            return ResponseEntity.ok(player);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Поиск свободных арен
    @GetMapping("/venues/available")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> findAvailableVenues(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime date) {
        try {
            List<Venue> venues = tournamentService.findAvailableVenues(date);
            return ResponseEntity.ok(venues);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. Создание расписания тура
    @PostMapping("/schedule/round")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateRoundSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime roundDate,
            @RequestParam Long venueId,
            @RequestBody List<Long> teamIds) {
        try {
            List<Match> matches = tournamentService.generateRoundSchedule(teamIds, roundDate, venueId);
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}