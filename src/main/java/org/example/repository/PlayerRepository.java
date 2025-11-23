package org.example.repository;

import org.example.model.Player;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class PlayerRepository {
    private final Map<Long, Player> players = new HashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    public List<Player> findAll() {
        return new ArrayList<>(players.values());
    }

    public Optional<Player> findById(Long id) {
        return Optional.ofNullable(players.get(id));
    }

    public List<Player> findByTeamId(Long teamId) {
        return players.values().stream()
                .filter(player -> player.getTeamId().equals(teamId))
                .collect(Collectors.toList());
    }

    public Player save(Player player) {
        if (player.getId() == null) {
            player.setId(counter.getAndIncrement());
        }
        players.put(player.getId(), player);
        return player;
    }

    public void deleteById(Long id) {
        players.remove(id);
    }
}