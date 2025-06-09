package ru.arklual.telegramparser.registries;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StatesRegistry {
    private final Map<String, String> states = new ConcurrentHashMap<>();
    public void setState(String teamId, String state) {
        states.put(teamId, state);
    }
    public String getState(String teamId) {
        return states.get(teamId);
    }
}
