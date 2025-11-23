package org.example.repository;

import org.example.model.Venue;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class VenueRepository {
    private final Map<Long, Venue> venues = new HashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    public List<Venue> findAll() {
        return new ArrayList<>(venues.values());
    }

    public Optional<Venue> findById(Long id) {
        return Optional.ofNullable(venues.get(id));
    }

    public Venue save(Venue venue) {
        if (venue.getId() == null) {
            venue.setId(counter.getAndIncrement());
        }
        venues.put(venue.getId(), venue);
        return venue;
    }

    public void deleteById(Long id) {
        venues.remove(id);
    }
}