package ru.arklual.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.arklual.crm.entity.TeamMember;
import ru.arklual.crm.entity.TeamRole;

import java.util.List;
import java.util.UUID;

public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    boolean existsByTeamIdAndUserIdAndRole(UUID teamId, UUID id, TeamRole teamRole);
    List<TeamMember> findByUserId(UUID id);
}
