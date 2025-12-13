package org.example.controller;

import org.example.model.Standing;
import org.example.service.StandingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/standings")
public class StandingController {

    private final StandingService standingService;

    public StandingController(StandingService standingService) {
        this.standingService = standingService;
    }

    // 1. Полуть всю турнирную таблицу
    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Standing> getAllStandings() {
        return standingService.getAllStandings();
    }

    // 2. Получить топ команд
    @GetMapping("/top")
    @PreAuthorize("permitAll()")
    public List<Standing> getTopStandings(@RequestParam(required = false) Integer limit) {
        return standingService.getTopStandings(limit);
    }

    // 3. Получить запись по ID
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Standing> getStandingById(@PathVariable Long id) {
        return standingService.getStandingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. Получить запись по команде
    @GetMapping("/team/{teamId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Standing> getStandingByTeamId(@PathVariable Long teamId) {
        return standingService.getStandingByTeamId(teamId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 5. Получить позицию команды
    @GetMapping("/team/{teamId}/position")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getPositionByTeamId(@PathVariable Long teamId) {
        try {
            Integer position = standingService.getPositionByTeamId(teamId);
            return ResponseEntity.ok(Map.of("teamId", teamId, "position", position));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 6. Создать запись
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createStanding(@RequestBody Standing standing) {
        try {
            Standing createdStanding = standingService.createStanding(standing);
            return ResponseEntity.ok(createdStanding);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 7. Обновить запись
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStanding(@PathVariable Long id, @RequestBody Standing standingDetails) {
        try {
            Standing updatedStanding = standingService.updateStanding(id, standingDetails);
            return ResponseEntity.ok(updatedStanding);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 8. Удалить запись
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteStanding(@PathVariable Long id) {
        try {
            standingService.deleteStanding(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // === БИЗНЕС-ОПЕРАЦИИ ===

    // 9. Обновить все позиции (пересчитать таблицу)
    @PostMapping("/update-positions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePositions() {
        try {
            standingService.updatePositions();
            return ResponseEntity.ok("Standings positions updated");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 10. Сбросить все статистики
    @PostMapping("/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetAllStandings() {
        try {
            standingService.resetAllStandings();
            return ResponseEntity.ok("All standings reset to zero");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 11. Получить статистику лиги
    @GetMapping("/league/stats")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getLeagueStats() {
        try {
            Map<String, Object> stats = standingService.getLeagueStats();
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 12. Прогноз чемпиона
    @GetMapping("/predict-champion")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> predictChampion() {
        try {
            Map<String, Object> prediction = standingService.predictChampion();
            return ResponseEntity.ok(prediction);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 13. Удалить запись по команде
    @DeleteMapping("/team/{teamId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteByTeamId(@PathVariable Long teamId) {
        try {
            standingService.deleteByTeamId(teamId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}