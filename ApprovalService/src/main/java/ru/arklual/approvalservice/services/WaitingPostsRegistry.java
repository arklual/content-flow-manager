package ru.arklual.approvalservice.services;

import org.springframework.stereotype.Service;
import ru.arklual.approvalservice.dto.protobuf.PostProto;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WaitingPostsRegistry {

    private final ConcurrentHashMap<String, Set<PostProto.Post>> waitingPosts;

    public WaitingPostsRegistry(Set<PostProto.Post> waitingPosts) {
        this.waitingPosts = new ConcurrentHashMap<>();
        for (PostProto.Post post : waitingPosts) {
            this.waitingPosts
                    .computeIfAbsent(post.getTeamId(), k -> ConcurrentHashMap.newKeySet())
                    .add(post);
        }
    }

    public Set<PostProto.Post> getPosts(String teamId) {
        return waitingPosts.computeIfAbsent(teamId, k -> ConcurrentHashMap.newKeySet());
    }


}
