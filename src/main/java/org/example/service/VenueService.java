package org.example.service;

import org.example.model.Venue;
import org.example.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class VenueService {

    @Autowired
    private VenueRepository venueRepository;

    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    public Optional<Venue> getVenueById(Long id) {
        return venueRepository.findById(id);
    }

    public Optional<Venue> getVenueByName(String name) {
        return venueRepository.findByName(name);
    }

    public List<Venue> getVenuesByCity(String city) {
        return venueRepository.findByCity(city);
    }

    public List<Venue> getVenuesByCapacity(Integer minCapacity) {
        return venueRepository.findByCapacityGreaterThanEqual(minCapacity);
    }

    public List<Venue> getLargestVenues(Integer limit) {
        List<Venue> allVenues = venueRepository.findAllByOrderByCapacityDesc();
        return limit != null && limit < allVenues.size() ?
                allVenues.subList(0, limit) : allVenues;
    }

    public boolean existsById(Long id) {
        return venueRepository.existsById(id);
    }

    @Transactional
    public Venue createVenue(Venue venue) {
        // Проверка уникальности имени
        if (venueRepository.findByName(venue.getName()).isPresent()) {
            throw new RuntimeException("Venue with name '" + venue.getName() + "' already exists");
        }

        // Валидация вместимости
        if (venue.getCapacity() != null && venue.getCapacity() <= 0) {
            throw new RuntimeException("Capacity must be positive");
        }

        return venueRepository.save(venue);
    }

    @Transactional
    public Venue updateVenue(Long id, Venue venueDetails) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venue not found with ID: " + id));

        // Проверка уникальности имени (если меняется)
        if (venueDetails.getName() != null &&
                !venueDetails.getName().equals(venue.getName()) &&
                venueRepository.findByName(venueDetails.getName()).isPresent()) {
            throw new RuntimeException("Venue with name '" + venueDetails.getName() + "' already exists");
        }

        if (venueDetails.getName() != null) {
            venue.setName(venueDetails.getName());
        }
        if (venueDetails.getCity() != null) {
            venue.setCity(venueDetails.getCity());
        }
        if (venueDetails.getCapacity() != null) {
            if (venueDetails.getCapacity() <= 0) {
                throw new RuntimeException("Capacity must be positive");
            }
            venue.setCapacity(venueDetails.getCapacity());
        }
        if (venueDetails.getFieldType() != null) {
            venue.setFieldType(venueDetails.getFieldType());
        }

        return venueRepository.save(venue);
    }

    @Transactional
    public void deleteVenue(Long id) {
        if (!venueRepository.existsById(id)) {
            throw new RuntimeException("Venue not found with ID: " + id);
        }

        venueRepository.deleteById(id);
    }

    // Бизнес-операция: Найти подходящие арены для матча
    public List<Venue> findSuitableVenues(String city, Integer minCapacity) {
        if (city == null && minCapacity == null) {
            return getAllVenues();
        }

        if (city != null && minCapacity != null) {
            return venueRepository.findByCityAndMinCapacity(city, minCapacity);
        } else if (city != null) {
            return venueRepository.findByCity(city);
        } else {
            return venueRepository.findByCapacityGreaterThanEqual(minCapacity);
        }
    }

    // Бизнес-операция: Увеличить вместимость арены
    @Transactional
    public Venue expandVenueCapacity(Long venueId, Integer additionalCapacity) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found with ID: " + venueId));

        if (additionalCapacity == null || additionalCapacity <= 0) {
            throw new RuntimeException("Additional capacity must be positive");
        }

        venue.setCapacity(venue.getCapacity() + additionalCapacity);
        return venueRepository.save(venue);
    }

    // Бизнес-операция: Получить статистику арен
    public Map<String, Object> getVenueStatistics() {
        List<Venue> allVenues = getAllVenues();

        if (allVenues.isEmpty()) {
            return Map.of(
                    "totalVenues", 0,
                    "totalCapacity", 0,
                    "averageCapacity", 0.0,
                    "largestVenue", "No venues"
            );
        }

        int totalCapacity = allVenues.stream()
                .mapToInt(Venue::getCapacity)
                .sum();

        double averageCapacity = (double) totalCapacity / allVenues.size();

        Optional<Venue> largestVenue = allVenues.stream()
                .max((v1, v2) -> Integer.compare(v1.getCapacity(), v2.getCapacity()));

        // Распределение по городам
        Map<String, Long> venuesByCity = allVenues.stream()
                .filter(v -> v.getCity() != null)
                .collect(Collectors.groupingBy(Venue::getCity, Collectors.counting()));

        // Распределение по типу покрытия
        Map<String, Long> venuesByFieldType = allVenues.stream()
                .filter(v -> v.getFieldType() != null)
                .collect(Collectors.groupingBy(Venue::getFieldType, Collectors.counting()));

        return Map.of(
                "totalVenues", allVenues.size(),
                "totalCapacity", totalCapacity,
                "averageCapacity", Math.round(averageCapacity * 100.0) / 100.0,
                "largestVenue", largestVenue.map(v -> v.getName() + " (" + v.getCapacity() + " seats)").orElse("Unknown"),
                "venuesByCity", venuesByCity,
                "venuesByFieldType", venuesByFieldType
        );
    }

    // Бизнес-операция: Поиск арен по части названия
    public List<Venue> searchVenuesByName(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllVenues();
        }

        return venueRepository.findByNameContainingIgnoreCase(searchTerm.trim());
    }
}