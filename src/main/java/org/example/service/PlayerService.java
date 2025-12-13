package org.example.service;

import org.example.model.Player;
import org.example.repository.PlayerRepository;
import org.example.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public Optional<Player> getPlayerById(Long id) {
        return playerRepository.findById(id);
    }

    public List<Player> getPlayersByTeam(Long teamId) {
        return playerRepository.findByTeamId(teamId);
    }

    public List<Player> getPlayersByPosition(String position) {
        return playerRepository.findByPosition(position);
    }

    public List<Player> getTopScorers(Integer limit) {
        List<Player> allPlayers = playerRepository.findAllByOrderByGoalsScoredDesc();
        return limit != null && limit < allPlayers.size() ?
                allPlayers.subList(0, limit) : allPlayers;
    }

    public boolean existsByTeamIdAndJerseyNumber(Long teamId, Integer jerseyNumber) {
        return playerRepository.existsByTeamIdAndJerseyNumber(teamId, jerseyNumber);
    }

    @Transactional
    public Player createPlayer(Player player) {
        // Проверка существования команды
        if (!teamRepository.existsById(player.getTeamId())) {
            throw new RuntimeException("Team not found with ID: " + player.getTeamId());
        }

        // Проверка уникальности номера в команде
        if (player.getJerseyNumber() != null &&
                playerRepository.existsByTeamIdAndJerseyNumber(player.getTeamId(), player.getJerseyNumber())) {
            throw new RuntimeException("Jersey number " + player.getJerseyNumber() +
                    " is already taken in this team");
        }

        // Установка значений по умолчанию
        if (player.getGoalsScored() == null) {
            player.setGoalsScored(0);
        }

        return playerRepository.save(player);
    }

    @Transactional
    public Player updatePlayer(Long id, Player playerDetails) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + id));

        // Проверка команды (если меняется)
        if (playerDetails.getTeamId() != null &&
                !playerDetails.getTeamId().equals(player.getTeamId())) {
            if (!teamRepository.existsById(playerDetails.getTeamId())) {
                throw new RuntimeException("Team not found with ID: " + playerDetails.getTeamId());
            }
            player.setTeamId(playerDetails.getTeamId());
        }

        // Проверка номера (если меняется)
        if (playerDetails.getJerseyNumber() != null &&
                !playerDetails.getJerseyNumber().equals(player.getJerseyNumber())) {
            if (playerRepository.existsByTeamIdAndJerseyNumber(
                    player.getTeamId(), playerDetails.getJerseyNumber())) {
                throw new RuntimeException("Jersey number " + playerDetails.getJerseyNumber() +
                        " is already taken in this team");
            }
            player.setJerseyNumber(playerDetails.getJerseyNumber());
        }

        if (playerDetails.getName() != null) {
            player.setName(playerDetails.getName());
        }
        if (playerDetails.getPosition() != null) {
            player.setPosition(playerDetails.getPosition());
        }
        if (playerDetails.getAge() != null) {
            player.setAge(playerDetails.getAge());
        }
        if (playerDetails.getGoalsScored() != null) {
            player.setGoalsScored(playerDetails.getGoalsScored());
        }

        return playerRepository.save(player);
    }

    @Transactional
    public void deletePlayer(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new RuntimeException("Player not found with ID: " + id);
        }

        playerRepository.deleteById(id);
    }

    // Бизнес-операция: Забить гол
    @Transactional
    public Player scoreGoal(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + playerId));

        player.setGoalsScored(player.getGoalsScored() + 1);
        return playerRepository.save(player);
    }

    // Бизнес-операция: Перевести игрока в другую команду
    @Transactional
    public Player transferPlayer(Long playerId, Long newTeamId, Integer newJerseyNumber) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + playerId));

        if (!teamRepository.existsById(newTeamId)) {
            throw new RuntimeException("New team not found with ID: " + newTeamId);
        }

        // Проверка номера в новой команде
        if (newJerseyNumber != null &&
                playerRepository.existsByTeamIdAndJerseyNumber(newTeamId, newJerseyNumber)) {
            throw new RuntimeException("Jersey number " + newJerseyNumber +
                    " is already taken in the new team");
        }

        player.setTeamId(newTeamId);
        if (newJerseyNumber != null) {
            player.setJerseyNumber(newJerseyNumber);
        }

        return playerRepository.save(player);
    }

    // Бизнес-операция: Получить статистику команды по игрокам
    public Map<String, Object> getTeamPlayerStatistics(Long teamId) {
        List<Player> players = getPlayersByTeam(teamId);

        if (players.isEmpty()) {
            return Map.of(
                    "totalPlayers", 0,
                    "averageAge", 0.0,
                    "totalGoals", 0,
                    "topScorer", "No players"
            );
        }

        double averageAge = players.stream()
                .mapToInt(Player::getAge)
                .average()
                .orElse(0.0);

        int totalGoals = players.stream()
                .mapToInt(Player::getGoalsScored)
                .sum();

        Optional<Player> topScorer = players.stream()
                .max((p1, p2) -> Integer.compare(p1.getGoalsScored(), p2.getGoalsScored()));

        // Распределение по позициям
        Map<String, Long> positionDistribution = players.stream()
                .filter(p -> p.getPosition() != null)
                .collect(Collectors.groupingBy(Player::getPosition, Collectors.counting()));

        return Map.of(
                "totalPlayers", players.size(),
                "averageAge", Math.round(averageAge * 100.0) / 100.0,
                "totalGoals", totalGoals,
                "topScorer", topScorer.map(p -> p.getName() + " (" + p.getGoalsScored() + " goals)").orElse("No scorer"),
                "positionDistribution", positionDistribution
        );
    }

    // Бизнес-операция: Обновить возраст всех игроков (имитация нового сезона)
    @Transactional
    public void incrementAllPlayersAge() {
        List<Player> allPlayers = getAllPlayers();
        allPlayers.forEach(player -> {
            player.setAge(player.getAge() + 1);
            playerRepository.save(player);
        });
    }
}