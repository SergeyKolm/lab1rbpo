package org.example.controller;

import org.example.model.Player;
import org.example.service.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    // 1. Получить всех игроков
    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Player> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    // 2. Получить игрока по ID
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        return playerService.getPlayerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Получить игроков команды
    @GetMapping("/team/{teamId}")
    @PreAuthorize("permitAll()")
    public List<Player> getPlayersByTeam(@PathVariable Long teamId) {
        return playerService.getPlayersByTeam(teamId);
    }

    // 4. Получить игроков по позиции
    @GetMapping("/position/{position}")
    @PreAuthorize("permitAll()")
    public List<Player> getPlayersByPosition(@PathVariable String position) {
        return playerService.getPlayersByPosition(position);
    }

    // 5. Получить топ бомбардиров
    @GetMapping("/top-scorers")
    @PreAuthorize("permitAll()")
    public List<Player> getTopScorers(@RequestParam(required = false) Integer limit) {
        return playerService.getTopScorers(limit);
    }

    // 6. Создать игрока
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPlayer(@RequestBody Player player) {
        try {
            Player createdPlayer = playerService.createPlayer(player);
            return ResponseEntity.ok(createdPlayer);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 7. Обновить игрока
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePlayer(@PathVariable Long id, @RequestBody Player playerDetails) {
        try {
            Player updatedPlayer = playerService.updatePlayer(id, playerDetails);
            return ResponseEntity.ok(updatedPlayer);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 8. Удалить игрока
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePlayer(@PathVariable Long id) {
        try {
            playerService.deletePlayer(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // === БИЗНЕС-ОПЕРАЦИИ ===

    // 9. Забить гол (увеличить счетчик голов)
    @PostMapping("/{id}/score-goal")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> scoreGoal(@PathVariable Long id) {
        try {
            Player updatedPlayer = playerService.scoreGoal(id);
            return ResponseEntity.ok(updatedPlayer);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 10. Перевести игрока в другую команду
    @PostMapping("/{id}/transfer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> transferPlayer(@PathVariable Long id,
                                            @RequestParam Long newTeamId,
                                            @RequestParam(required = false) Integer newJerseyNumber) {
        try {
            Player transferredPlayer = playerService.transferPlayer(id, newTeamId, newJerseyNumber);
            return ResponseEntity.ok(transferredPlayer);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 11. Получить статистику команды по игрокам
    @GetMapping("/team/{teamId}/statistics")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getTeamPlayerStatistics(@PathVariable Long teamId) {
        try {
            Map<String, Object> statistics = playerService.getTeamPlayerStatistics(teamId);
            return ResponseEntity.ok(statistics);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 12. Обновить возраст всех игроков (новый сезон)
    @PostMapping("/increment-age")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> incrementAllPlayersAge() {
        try {
            playerService.incrementAllPlayersAge();
            return ResponseEntity.ok("All players' ages incremented by 1");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 13. Проверить доступность номера в команде
    @GetMapping("/check-jersey")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> checkJerseyAvailability(@RequestParam Long teamId,
                                                     @RequestParam Integer jerseyNumber) {
        try {
            boolean isTaken = playerService.existsByTeamIdAndJerseyNumber(teamId, jerseyNumber);
            return ResponseEntity.ok(Map.of(
                    "teamId", teamId,
                    "jerseyNumber", jerseyNumber,
                    "available", !isTaken
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}