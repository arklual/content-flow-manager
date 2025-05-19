package ru.arklual.telegramparser.service;

import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TelegramClientManager implements DisposableBean {

***REMOVED******REMOVED***private final SimpleTelegramClientFactory clientFactory;
***REMOVED******REMOVED***private final Map<String, SimpleTelegramClient> clients = new ConcurrentHashMap<>();

***REMOVED******REMOVED***public TelegramClientManager(SimpleTelegramClientFactory clientFactory) {
***REMOVED******REMOVED******REMOVED******REMOVED***this.clientFactory = clientFactory;
***REMOVED******REMOVED***}

***REMOVED******REMOVED***public SimpleTelegramClient getClient(String teamId) {
***REMOVED******REMOVED******REMOVED******REMOVED***return clients.get(teamId);
***REMOVED******REMOVED***}

***REMOVED******REMOVED***public void stopClient(String teamId) {
***REMOVED******REMOVED******REMOVED******REMOVED***SimpleTelegramClient client = clients.remove(teamId);
***REMOVED******REMOVED******REMOVED******REMOVED***if (client != null) {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***try {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***client.close();
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***} catch (Exception e) {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***e.printStackTrace();
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***}
***REMOVED******REMOVED******REMOVED******REMOVED***}
***REMOVED******REMOVED***}

***REMOVED******REMOVED***public List<String> getAllTeamIds() {
***REMOVED******REMOVED******REMOVED******REMOVED***return new ArrayList<>(clients.keySet());
***REMOVED******REMOVED***}

***REMOVED******REMOVED***@Override
***REMOVED******REMOVED***public void destroy() throws Exception {
***REMOVED******REMOVED******REMOVED******REMOVED***System.out.println("Shutting down all Telegram clients...");
***REMOVED******REMOVED******REMOVED******REMOVED***clients.values().forEach(client -> {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***try {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***client.close();
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***} catch (Exception e) {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***e.printStackTrace();
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***}
***REMOVED******REMOVED******REMOVED******REMOVED***});
***REMOVED******REMOVED******REMOVED******REMOVED***clients.clear();
***REMOVED******REMOVED******REMOVED******REMOVED***System.out.println("All Telegram clients stopped.");
***REMOVED******REMOVED***}

***REMOVED******REMOVED***public void startClientViaQr(String teamId, TDLibSettings settings, QrCodeRegistry qrRegistry) {
***REMOVED******REMOVED******REMOVED******REMOVED***SimpleTelegramClientBuilder builder = clientFactory.builder(settings);
***REMOVED******REMOVED******REMOVED******REMOVED***builder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, update -> {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***TdApi.AuthorizationState state = update.authorizationState;
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***if (state instanceof TdApi.AuthorizationStateWaitOtherDeviceConfirmation) {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) state).link;
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***qrRegistry.setQrCode(teamId, link);
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***}
***REMOVED******REMOVED******REMOVED******REMOVED***});
***REMOVED******REMOVED******REMOVED******REMOVED***SimpleTelegramClient client = builder.build(AuthenticationSupplier.qrCode());
***REMOVED******REMOVED******REMOVED******REMOVED***clients.put(teamId, client);
***REMOVED******REMOVED***}
}
