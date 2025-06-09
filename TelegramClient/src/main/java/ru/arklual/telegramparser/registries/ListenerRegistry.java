package ru.arklual.telegramparser.registries;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ListenerRegistry {

    private final Map<String, ConcurrentHashMap<String, Integer>> clients;

    public ListenerRegistry() {
        clients = new ConcurrentHashMap<>();
    }

    public boolean isListenerExist(String teamId, String chatId) {
        if (!clients.containsKey(teamId)) {
            return false;
        }
        return clients.get(teamId).containsKey(chatId);
    }

    public void addListener(String teamId, String chatId) {
        if (!clients.containsKey(teamId)) {
            clients.put(teamId, new ConcurrentHashMap<>());
        }
        clients.get(teamId).put(chatId, 1);
    }

}
