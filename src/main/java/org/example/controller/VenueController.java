package org.example.controller;

import org.example.model.Venue;
import org.example.service.VenueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    // 1. Получить все арены
    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Venue> getAllVenues() {
        return venueService.getAllVenues();
    }

    // 2. Получить арену по ID
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Venue> getVenueById(@PathVariable Long id) {
        return venueService.getVenueById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Получить арену по названию
    @GetMapping("/name/{name}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Venue> getVenueByName(@PathVariable String name) {
        return venueService.getVenueByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. Получить арены по городу
    @GetMapping("/city/{city}")
    @PreAuthorize("permitAll()")
    public List<Venue> getVenuesByCity(@PathVariable String city) {
        return venueService.getVenuesByCity(city);
    }

    // 5. Получить арены по минимальной вместимости
    @GetMapping("/capacity")
    @PreAuthorize("permitAll()")
    public List<Venue> getVenuesByCapacity(@RequestParam Integer minCapacity) {
        return venueService.getVenuesByCapacity(minCapacity);
    }

    // 6. Получить самые большие арены
    @GetMapping("/largest")
    @PreAuthorize("permitAll()")
    public List<Venue> getLargestVenues(@RequestParam(required = false) Integer limit) {
        return venueService.getLargestVenues(limit);
    }

    // 7. Создать арену
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createVenue(@RequestBody Venue venue) {
        try {
            Venue createdVenue = venueService.createVenue(venue);
            return ResponseEntity.ok(createdVenue);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 8. Обновить арену
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateVenue(@PathVariable Long id, @RequestBody Venue venueDetails) {
        try {
            Venue updatedVenue = venueService.updateVenue(id, venueDetails);
            return ResponseEntity.ok(updatedVenue);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 9. Удалить арену
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteVenue(@PathVariable Long id) {
        try {
            venueService.deleteVenue(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // === БИЗНЕС-ОПЕРАЦИИ ===

    // 10. Найти подходящие арены
    @GetMapping("/suitable")
    @PreAuthorize("permitAll()")
    public List<Venue> findSuitableVenues(@RequestParam(required = false) String city,
                                          @RequestParam(required = false) Integer minCapacity) {
        return venueService.findSuitableVenues(city, minCapacity);
    }

    // 11. Увеличить вместимость арены
    @PostMapping("/{id}/expand")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> expandVenueCapacity(@PathVariable Long id,
                                                 @RequestParam Integer additionalCapacity) {
        try {
            Venue expandedVenue = venueService.expandVenueCapacity(id, additionalCapacity);
            return ResponseEntity.ok(expandedVenue);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 12. Получить статистику арен
    @GetMapping("/statistics")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getVenueStatistics() {
        try {
            Map<String, Object> statistics = venueService.getVenueStatistics();
            return ResponseEntity.ok(statistics);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 13. Поиск арен по части названия
    @GetMapping("/search")
    @PreAuthorize("permitAll()")
    public List<Venue> searchVenuesByName(@RequestParam String searchTerm) {
        return venueService.searchVenuesByName(searchTerm);
    }

    // 14. Проверить существование арены по имени
    @GetMapping("/exists/{name}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> checkVenueExists(@PathVariable String name) {
        boolean exists = venueService.getVenueByName(name).isPresent();
        return ResponseEntity.ok(Map.of("exists", exists, "venueName", name));
    }
}