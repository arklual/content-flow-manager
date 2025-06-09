package ru.arklual.telegramparser.registries;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class QrCodeRegistry {
    private final Map<String, String> qrCodes = new ConcurrentHashMap<>();
    public void setQrCode(String teamId, String link) {
        qrCodes.put(teamId, link);
    }
    public String getQrCode(String teamId) {
        return qrCodes.get(teamId);
    }
}
