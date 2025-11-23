package org.example.service;

import org.example.model.Standing;
import org.example.repository.StandingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StandingService {

    @Autowired
    private StandingRepository standingRepository;

    public List<Standing> getAllStandings() {
        return standingRepository.findAll();
    }

    public Optional<Standing> getStandingById(Long id) {
        return standingRepository.findById(id);
    }

    public Optional<Standing> getStandingByTeamId(Long teamId) {
        return standingRepository.findByTeamId(teamId);
    }

    public Standing createStanding(Standing standing) {
        return standingRepository.save(standing);
    }

    public Standing updateStanding(Long id, Standing standingDetails) {
        Standing standing = standingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Standing not found"));
        standing.setTeamId(standingDetails.getTeamId());
        standing.setPlayed(standingDetails.getPlayed());
        standing.setWins(standingDetails.getWins());
        standing.setDraws(standingDetails.getDraws());
        standing.setLosses(standingDetails.getLosses());
        standing.setGoalsFor(standingDetails.getGoalsFor());
        standing.setGoalsAgainst(standingDetails.getGoalsAgainst());
        standing.setPoints(standingDetails.getPoints());
        return standingRepository.save(standing);
    }

    public void deleteStanding(Long id) {
        standingRepository.deleteById(id);
    }
}