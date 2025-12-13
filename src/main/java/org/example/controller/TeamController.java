package org.example.controller;

import org.example.model.Team;
import org.example.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    // 1. Получить все команды
    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Team> getAllTeams() {
        return teamService.getAllTeams();
    }

    // 2. Получить команду по ID
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Team> getTeamById(@PathVariable Long id) {
        return teamService.getTeamById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Получить команду по названию
    @GetMapping("/name/{name}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Team> getTeamByName(@PathVariable String name) {
        return teamService.getTeamByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. Получить команды по городу
    @GetMapping("/city/{city}")
    @PreAuthorize("permitAll()")
    public List<Team> getTeamsByCity(@PathVariable String city) {
        return teamService.getTeamsByCity(city);
    }

    // 5. Получить топ команд
    @GetMapping("/top")
    @PreAuthorize("permitAll()")
    public List<Team> getTopTeams(@RequestParam(required = false) Integer limit) {
        return teamService.getTopTeams(limit);
    }

    // 6. Создать команду
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTeam(@RequestBody Team team) {
        try {
            Team createdTeam = teamService.createTeam(team);
            return ResponseEntity.ok(createdTeam);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 7. Обновить команду
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTeam(@PathVariable Long id, @RequestBody Team teamDetails) {
        try {
            Team updatedTeam = teamService.updateTeam(id, teamDetails);
            return ResponseEntity.ok(updatedTeam);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 8. Удалить команду
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTeam(@PathVariable Long id) {
        try {
            teamService.deleteTeam(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // === БИЗНЕС-ОПЕРАЦИИ ===

    // 9. Добавить очки команде
    @PostMapping("/{id}/add-points")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addPoints(@PathVariable Long id,
                                       @RequestParam Integer points) {
        try {
            Team updatedTeam = teamService.addPoints(id, points);
            return ResponseEntity.ok(updatedTeam);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 10. Переместить команду в другой город
    @PostMapping("/{id}/relocate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> relocateTeam(@PathVariable Long id,
                                          @RequestParam String newCity) {
        try {
            Team relocatedTeam = teamService.relocateTeam(id, newCity);
            return ResponseEntity.ok(relocatedTeam);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 11. Получить средний возраст команды
    @GetMapping("/{id}/average-age")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getTeamAverageAge(@PathVariable Long id) {
        try {
            Double averageAge = teamService.getTeamAverageAge(id);
            return ResponseEntity.ok(Map.of("averageAge", averageAge));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 12. Получить статистику лиги
    @GetMapping("/league/statistics")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getLeagueStatistics() {
        try {
            Map<String, Object> statistics = teamService.getLeagueStatistics();
            return ResponseEntity.ok(statistics);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 13. Проверить существование команды по имени
    @GetMapping("/exists/{name}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> checkTeamExists(@PathVariable String name) {
        boolean exists = teamService.existsByName(name);
        return ResponseEntity.ok(Map.of("exists", exists, "teamName", name));
    }
}