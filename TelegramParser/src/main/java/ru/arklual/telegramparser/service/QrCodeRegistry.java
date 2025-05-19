package ru.arklual.telegramparser.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class QrCodeRegistry {
***REMOVED******REMOVED***private final Map<String, String> qrCodes = new ConcurrentHashMap<>();
***REMOVED******REMOVED***public void setQrCode(String teamId, String link) {
***REMOVED******REMOVED******REMOVED******REMOVED***qrCodes.put(teamId, link);
***REMOVED******REMOVED***}
***REMOVED******REMOVED***public String getQrCode(String teamId) {
***REMOVED******REMOVED******REMOVED******REMOVED***return qrCodes.get(teamId);
***REMOVED******REMOVED***}
}
