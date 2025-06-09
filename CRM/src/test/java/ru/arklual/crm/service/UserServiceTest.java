package ru.arklual.crm.service;

import org.junit.jupiter.api.Test;
import ru.arklual.crm.dto.responses.UserResponse;
import ru.arklual.crm.entity.Team;
import ru.arklual.crm.entity.TeamMember;
import ru.arklual.crm.entity.User;
import ru.arklual.crm.entity.UserStatus;
import ru.arklual.crm.exception.ResourceNotFoundException;
import ru.arklual.crm.repository.UserRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Test
    void isUserNotInTeam_shouldReturnTrue_ifUserNotFound() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.findByEmail("pupupu@example.com")).thenReturn(Optional.empty());

        UserService service = new UserService(repo);

        boolean result = service.isUserNotInTeam(UUID.randomUUID(), "pupupu@example.com");
        assertTrue(result);
    }

    @Test
    void isUserNotInTeam_shouldReturnFalse_ifUserInTeam() {
        UUID teamId = UUID.randomUUID();

        Team team = new Team();
        team.setId(teamId);

        TeamMember member = new TeamMember();
        member.setTeam(team);

        User user = new User();
        user.setTeamMemberships(Set.of(member));

        UserRepository repo = mock(UserRepository.class);
        when(repo.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        UserService service = new UserService(repo);

        boolean result = service.isUserNotInTeam(teamId, "user@example.com");
        assertFalse(result);
    }

    @Test
    void getUserByEmail_shouldReturnUserResponse_ifUserFound() {
        UUID id = UUID.randomUUID();

        User user = new User();
        user.setId(id);
        user.setEmail("XX_i_v_prod@example.com");
        user.setName("Krokodilo");
        user.setStatus(UserStatus.ACTIVE);

        UserRepository repo = mock(UserRepository.class);
        when(repo.findByEmail("XX_i_v_prod@example.com")).thenReturn(Optional.of(user));

        UserService service = new UserService(repo);

        UserResponse response = service.getUserByEmail("XX_i_v_prod@example.com");

        assertEquals("XX_i_v_prod@example.com", response.getEmail());
        assertEquals("Krokodilo", response.getName());
        assertEquals(UserStatus.ACTIVE, response.getStatus());
        assertEquals(id, response.getUuid());
    }

    @Test
    void getUserByEmail_shouldThrow_ifUserNotFound() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.findByEmail("feya-winks@example.com")).thenReturn(Optional.empty());

        UserService service = new UserService(repo);

        assertThrows(ResourceNotFoundException.class,
                () -> service.getUserByEmail("feya-winks@example.com"));
    }
}