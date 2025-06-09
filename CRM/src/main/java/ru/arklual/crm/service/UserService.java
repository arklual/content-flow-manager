package ru.arklual.crm.service;

import org.springframework.stereotype.Service;
import ru.arklual.crm.dto.responses.UserResponse;
import ru.arklual.crm.entity.TeamMember;
import ru.arklual.crm.entity.User;
import ru.arklual.crm.exception.ResourceNotFoundException;
import ru.arklual.crm.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isUserNotInTeam(UUID teamId, String email) {
        Optional<User> mayBeUser = userRepository.findByEmail(email);
        if (mayBeUser.isEmpty()) {
            return true;
        }
        User user = mayBeUser.get();
        for (TeamMember teamMembership : user.getTeamMemberships()) {
            if (teamMembership.getTeam().getId().equals(teamId)) {
                return false;
            }
        }
        return true;
    }

    public UserResponse getUserByEmail(String email) {
        Optional<User> mayBeUser = userRepository.findByEmail(email);
        return mapUserToResponse(
                mayBeUser.orElseThrow(
                        () -> new ResourceNotFoundException("User not found")
                )
        );
    }

    private UserResponse mapUserToResponse(User user) {
        return UserResponse.builder()
                .name(user.getName())
                .uuid(user.getId())
                .status(user.getStatus())
                .email(user.getEmail())
                .build();
    }


}
