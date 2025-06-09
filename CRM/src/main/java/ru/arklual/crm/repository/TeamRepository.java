package ru.arklual.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.arklual.crm.entity.Team;
import ru.arklual.crm.entity.TeamStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {
    List<Team> findByStatus(TeamStatus status);
    boolean existsByName(String name);
}
