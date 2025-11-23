package org.example.controller;

import org.example.model.Standing;
import org.example.service.StandingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/standings")
public class StandingController {

    @Autowired
    private StandingService standingService;

    @GetMapping
    public List<Standing> getAllStandings() {
        return standingService.getAllStandings();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Standing> getStandingById(@PathVariable Long id) {
        return standingService.getStandingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<Standing> getStandingByTeamId(@PathVariable Long teamId) {
        return standingService.getStandingByTeamId(teamId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Standing createStanding(@RequestBody Standing standing) {
        return standingService.createStanding(standing);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Standing> updateStanding(@PathVariable Long id, @RequestBody Standing standingDetails) {
        try {
            Standing updatedStanding = standingService.updateStanding(id, standingDetails);
            return ResponseEntity.ok(updatedStanding);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStanding(@PathVariable Long id) {
        standingService.deleteStanding(id);
        return ResponseEntity.ok().build();
    }
}