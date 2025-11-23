package org.example.service;

import org.example.model.Match;
import org.example.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public Optional<Match> getMatchById(Long id) {
        return matchRepository.findById(id);
    }

    public Match createMatch(Match match) {
        // Проверка конфликта расписания
        LocalDateTime matchStart = match.getMatchDate();
        LocalDateTime matchEnd = matchStart.plusHours(2); // предполагаем, что матч длится 2 часа

        List<Match> conflicts = matchRepository.findConflictingMatches(
                match.getHomeTeamId(), matchStart, matchEnd);
        conflicts.addAll(matchRepository.findConflictingMatches(
                match.getAwayTeamId(), matchStart, matchEnd));

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Team has scheduling conflict");
        }

        return matchRepository.save(match);
    }

    public Match updateMatch(Long id, Match matchDetails) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        match.setHomeTeamId(matchDetails.getHomeTeamId());
        match.setAwayTeamId(matchDetails.getAwayTeamId());
        match.setVenueId(matchDetails.getVenueId());
        match.setMatchDate(matchDetails.getMatchDate());
        match.setHomeTeamScore(matchDetails.getHomeTeamScore());
        match.setAwayTeamScore(matchDetails.getAwayTeamScore());
        match.setStatus(matchDetails.getStatus());
        return matchRepository.save(match);
    }

    public void deleteMatch(Long id) {
        matchRepository.deleteById(id);
    }
}