package org.example.repository;

import org.example.model.Match;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MatchRepository {
    private final Map<Long, Match> matches = new HashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    public List<Match> findAll() {
        return new ArrayList<>(matches.values());
    }

    public Optional<Match> findById(Long id) {
        return Optional.ofNullable(matches.get(id));
    }

    public Match save(Match match) {
        if (match.getId() == null) {
            match.setId(counter.getAndIncrement());
        }
        matches.put(match.getId(), match);
        return match;
    }

    public void deleteById(Long id) {
        matches.remove(id);
    }

    // Проверка конфликта матчей для команды
    public List<Match> findConflictingMatches(Long teamId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Match> conflicts = new ArrayList<>();
        for (Match match : matches.values()) {
            if ((match.getHomeTeamId().equals(teamId) || match.getAwayTeamId().equals(teamId)) &&
                    match.getMatchDate().isAfter(startTime) && match.getMatchDate().isBefore(endTime)) {
                conflicts.add(match);
            }
        }
        return conflicts;
    }
}