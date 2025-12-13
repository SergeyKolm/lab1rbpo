package org.example.service;

import org.example.model.Team;
import org.example.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StandingService standingService;

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Optional<Team> getTeamById(Long id) {
        return teamRepository.findById(id);
    }

    public Optional<Team> getTeamByName(String name) {
        return teamRepository.findByName(name);
    }

    public boolean existsById(Long id) {
        return teamRepository.existsById(id);
    }

    public boolean existsByName(String name) {
        return teamRepository.existsByName(name);
    }

    public List<Team> getTeamsByCity(String city) {
        return teamRepository.findByCity(city);
    }

    public List<Team> getTopTeams(Integer limit) {
        List<Team> allTeams = teamRepository.findAllByOrderByPointsDesc();
        return limit != null && limit < allTeams.size() ?
                allTeams.subList(0, limit) : allTeams;
    }

    @Transactional
    public Team createTeam(Team team) {
        // Проверка уникальности имени
        if (teamRepository.existsByName(team.getName())) {
            throw new RuntimeException("Team with name '" + team.getName() + "' already exists");
        }

        // Установка значений по умолчанию
        if (team.getPoints() == null) {
            team.setPoints(0);
        }

        Team savedTeam = teamRepository.save(team);

        // Создаем запись в турнирной таблице
        standingService.createStandingForTeam(savedTeam.getId());

        return savedTeam;
    }

    @Transactional
    public Team updateTeam(Long id, Team teamDetails) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found with ID: " + id));

        // Проверка уникальности имени (если меняется)
        if (teamDetails.getName() != null &&
                !teamDetails.getName().equals(team.getName()) &&
                teamRepository.existsByName(teamDetails.getName())) {
            throw new RuntimeException("Team with name '" + teamDetails.getName() + "' already exists");
        }

        if (teamDetails.getName() != null) {
            team.setName(teamDetails.getName());
        }
        if (teamDetails.getCity() != null) {
            team.setCity(teamDetails.getCity());
        }
        if (teamDetails.getCoachName() != null) {
            team.setCoachName(teamDetails.getCoachName());
        }
        if (teamDetails.getFoundationYear() != null) {
            team.setFoundationYear(teamDetails.getFoundationYear());
        }
        if (teamDetails.getPoints() != null) {
            team.setPoints(teamDetails.getPoints());
        }

        return teamRepository.save(team);
    }

    @Transactional
    public void deleteTeam(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new RuntimeException("Team not found with ID: " + id);
        }

        // Удаляем связанные данные
        standingService.deleteByTeamId(id);

        teamRepository.deleteById(id);
    }

    // Бизнес-операция: Добавить очки команде
    @Transactional
    public Team addPoints(Long teamId, Integer pointsToAdd) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with ID: " + teamId));

        if (pointsToAdd == null || pointsToAdd <= 0) {
            throw new RuntimeException("Points must be positive");
        }

        team.setPoints(team.getPoints() + pointsToAdd);
        return teamRepository.save(team);
    }

    // Бизнес-операция: Переместить команду в другой город
    @Transactional
    public Team relocateTeam(Long teamId, String newCity) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with ID: " + teamId));

        if (newCity == null || newCity.trim().isEmpty()) {
            throw new RuntimeException("City cannot be empty");
        }

        team.setCity(newCity);
        return teamRepository.save(team);
    }

    // Бизнес-операция: Получить средний возраст команды
    public Double getTeamAverageAge(Long teamId) {
        // Это потребует PlayerService, который мы создадим
        // Пока заглушка
        return 25.5;
    }

    // Бизнес-операция: Получить общую статистику лиги
    public Map<String, Object> getLeagueStatistics() {
        List<Team> allTeams = getAllTeams();
        int totalTeams = allTeams.size();
        int totalPoints = allTeams.stream().mapToInt(Team::getPoints).sum();
        double averagePoints = totalTeams > 0 ? (double) totalPoints / totalTeams : 0;

        // Самый старый и самый молодой клуб
        Optional<Team> oldestTeam = allTeams.stream()
                .filter(t -> t.getFoundationYear() != null)
                .min((t1, t2) -> t1.getFoundationYear().compareTo(t2.getFoundationYear()));

        Optional<Team> newestTeam = allTeams.stream()
                .filter(t -> t.getFoundationYear() != null)
                .max((t1, t2) -> t1.getFoundationYear().compareTo(t2.getFoundationYear()));

        // Город с наибольшим количеством команд
        Map<String, Long> teamsByCity = allTeams.stream()
                .filter(t -> t.getCity() != null)
                .collect(Collectors.groupingBy(Team::getCity, Collectors.counting()));

        Optional<Map.Entry<String, Long>> mostTeamsCity = teamsByCity.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        Map<String, Object> result = new HashMap<>();
        result.put("totalTeams", totalTeams);
        result.put("totalPoints", totalPoints);
        result.put("averagePoints", averagePoints);
        result.put("oldestTeam", oldestTeam.map(Team::getName).orElse("Unknown"));
        result.put("newestTeam", newestTeam.map(Team::getName).orElse("Unknown"));
        result.put("cityWithMostTeams", mostTeamsCity.map(e -> e.getKey() + " (" + e.getValue() + " teams)").orElse("Unknown"));

        return result;
    }
}