package ru.arklual.telegramparser.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.client.TDLibSettings;
import it.tdlight.jni.TdApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.arklual.telegramparser.dto.ClientStatusResponse;
import ru.arklual.telegramparser.dto.LinkResponse;
import ru.arklual.telegramparser.dto.MessageResponse;
import ru.arklual.telegramparser.dto.TelegramMessageDTO;
import ru.arklual.telegramparser.factories.TdSettingsFactory;
import ru.arklual.telegramparser.service.QrCodeRegistry;
import ru.arklual.telegramparser.service.TelegramClientManager;
import ru.arklual.telegramparser.service.TelegramKafkaProducer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/clients")
public class TelegramClientController {

***REMOVED******REMOVED***private final TelegramClientManager manager;
***REMOVED******REMOVED***private final TdSettingsFactory tdSettingsFactory;
***REMOVED******REMOVED***private final QrCodeRegistry qrCodeRegistry;
***REMOVED******REMOVED***private final TelegramKafkaProducer kafkaProducer;
***REMOVED******REMOVED***private final Map<String, Set<String>> listenersByTeam = new ConcurrentHashMap<>();

***REMOVED******REMOVED***public TelegramClientController(TelegramClientManager manager,
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***TdSettingsFactory tdSettingsFactory,
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***QrCodeRegistry qrCodeRegistry, TelegramKafkaProducer kafkaProducer) {
***REMOVED******REMOVED******REMOVED******REMOVED***this.manager = manager;
***REMOVED******REMOVED******REMOVED******REMOVED***this.tdSettingsFactory = tdSettingsFactory;
***REMOVED******REMOVED******REMOVED******REMOVED***this.qrCodeRegistry = qrCodeRegistry;
***REMOVED******REMOVED******REMOVED******REMOVED***this.kafkaProducer = kafkaProducer;
***REMOVED******REMOVED***}


***REMOVED******REMOVED***@DeleteMapping("/{teamId}")
***REMOVED******REMOVED***@Operation(
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***summary = "Stop running telegram client"
***REMOVED******REMOVED***)
***REMOVED******REMOVED***public ResponseEntity<MessageResponse> stopClient(@PathVariable String teamId) {
***REMOVED******REMOVED******REMOVED******REMOVED***manager.stopClient(teamId);
***REMOVED******REMOVED******REMOVED******REMOVED***return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse("Telegram client stopped for team " + teamId));
***REMOVED******REMOVED***}

***REMOVED******REMOVED***@GetMapping
***REMOVED******REMOVED***@Operation(
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***summary = "Get all team ids with running telegram client"
***REMOVED******REMOVED***)
***REMOVED******REMOVED***public List<String> listClients() {
***REMOVED******REMOVED******REMOVED******REMOVED***return manager.getAllTeamIds();
***REMOVED******REMOVED***}

***REMOVED******REMOVED***@GetMapping("/{teamId}/status")
***REMOVED******REMOVED***@Operation(
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***summary = "Get status of a Telegram client",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***description = "Returns the current authorization state of the Telegram client for the specified team.",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***responses = {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***@ApiResponse(
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***responseCode = "200",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***description = "Client is running",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***content = @Content(schema = @Schema(implementation = ClientStatusResponse.class))
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***),
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***@ApiResponse(
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***responseCode = "404",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***description = "Client not found",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***content = @Content(schema = @Schema(implementation = ClientStatusResponse.class))
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***)
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***}
***REMOVED******REMOVED***)
***REMOVED******REMOVED***public ResponseEntity<?> clientStatus(@PathVariable String teamId) {
***REMOVED******REMOVED******REMOVED******REMOVED***var client = manager.getClient(teamId);
***REMOVED******REMOVED******REMOVED******REMOVED***if (client == null) {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***return ResponseEntity.status(404)
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***.body(new ClientStatusResponse(teamId, "Not started", false));
***REMOVED******REMOVED******REMOVED******REMOVED***}
***REMOVED******REMOVED******REMOVED******REMOVED***try {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***TdApi.AuthorizationState state = client.send(new TdApi.GetAuthorizationState()).join();
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***String stateName = state.getClass().getSimpleName().replace("AuthorizationState", "");
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***return ResponseEntity.ok(new ClientStatusResponse(teamId, stateName, true));
***REMOVED******REMOVED******REMOVED******REMOVED***} catch (Exception e) {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***return ResponseEntity.status(500)
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***.body(new MessageResponse("Failed to get client status: " + e.getMessage()));
***REMOVED******REMOVED******REMOVED******REMOVED***}
***REMOVED******REMOVED***}

***REMOVED******REMOVED***@Operation(
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***summary = "Starts a new Telegram client",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***responses = {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***@ApiResponse(
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***responseCode = "409",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***description = "Client already exist"
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***),
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***@ApiResponse(
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***responseCode = "201",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***description = "Client is started"
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***)
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***}
***REMOVED******REMOVED***)
***REMOVED******REMOVED***@PostMapping("/{teamId}/start")
***REMOVED******REMOVED***public ResponseEntity<MessageResponse> startClientWithQr(@PathVariable String teamId) {
***REMOVED******REMOVED******REMOVED******REMOVED***if (manager.getClient(teamId) != null) {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse("Client already started"));
***REMOVED******REMOVED******REMOVED******REMOVED***}
***REMOVED******REMOVED******REMOVED******REMOVED***TDLibSettings settings = tdSettingsFactory.buildForTeam(teamId);
***REMOVED******REMOVED******REMOVED******REMOVED***manager.startClientViaQr(teamId, settings, qrCodeRegistry); 
***REMOVED******REMOVED******REMOVED******REMOVED***return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Telegram client started via QR for team " + teamId));
***REMOVED******REMOVED***}


***REMOVED******REMOVED***@GetMapping("/{teamId}/auth-link")
***REMOVED******REMOVED***@Operation(
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***summary = "Get QR code link for a team",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***description = "Returns a QR code link if it has been generated for the given team ID.",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***responses = {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***@ApiResponse(
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***responseCode = "200",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***description = "QR code link found",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***content = @Content(
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***mediaType = "application/json",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***schema = @Schema(implementation = LinkResponse.class)
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***)
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***),
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***@ApiResponse(
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***responseCode = "404",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***description = "QR code link not yet generated",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***content = @Content(
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***mediaType = "application/json",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***schema = @Schema(implementation = MessageResponse.class)
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***)
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***)
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***}
***REMOVED******REMOVED***)
***REMOVED******REMOVED***public ResponseEntity<?> getQr(@PathVariable String teamId) {
***REMOVED******REMOVED******REMOVED******REMOVED***String qrLink = qrCodeRegistry.getQrCode(teamId);
***REMOVED******REMOVED******REMOVED******REMOVED***if (qrLink == null) {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***return ResponseEntity.status(404).body(new MessageResponse("QR code not generated yet"));
***REMOVED******REMOVED******REMOVED******REMOVED***}
***REMOVED******REMOVED******REMOVED******REMOVED***return ResponseEntity.ok(new LinkResponse(qrLink));
***REMOVED******REMOVED***}


***REMOVED******REMOVED***@PostMapping("/{team_id}/add-listener")
***REMOVED******REMOVED***@Operation(
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***summary = "Add a listener to a chat or channel",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***description = "Registers an update listener for a specific Telegram channel or chat for the given team.",
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***parameters = {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***@Parameter(name = "team_id", description = "Team ID", required = true),
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***@Parameter(name = "channelId", description = "Telegram chat/channel ID", required = true)
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***},
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***responses = {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***@ApiResponse(responseCode = "201", description = "Listener added successfully"),
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***@ApiResponse(responseCode = "404", description = "Client not found", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***}
***REMOVED******REMOVED***)
***REMOVED******REMOVED***public ResponseEntity<?> addChannelOrChatListener(@PathVariable("team_id") String teamId,
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***@RequestParam String channelId) {
***REMOVED******REMOVED******REMOVED******REMOVED***listenersByTeam
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***.computeIfAbsent(teamId, k -> ConcurrentHashMap.newKeySet())
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***.add(channelId);
***REMOVED******REMOVED******REMOVED******REMOVED***SimpleTelegramClient client = manager.getClient(teamId);
***REMOVED******REMOVED******REMOVED******REMOVED***if (client == null) {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***return ResponseEntity.status(404)
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***.body(new MessageResponse("No client found for team " + teamId));
***REMOVED******REMOVED******REMOVED******REMOVED***}
***REMOVED******REMOVED******REMOVED******REMOVED***try {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***long targetChatId = Long.parseLong(channelId);
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***client.addUpdateHandler(TdApi.UpdateNewMessage.class, update -> {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***TdApi.Message message = update.message;
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***if (message.chatId == targetChatId) {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***TelegramMessageDTO dto = formatMessage(teamId, message);
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***kafkaProducer.sendMessage("telegram-messages", dto);
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***}
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***});
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Listener added for chat " + channelId));
***REMOVED******REMOVED******REMOVED******REMOVED***} catch (NumberFormatException e) {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***return ResponseEntity.badRequest()
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***.body(new MessageResponse("Invalid channel ID format: must be a number"));
***REMOVED******REMOVED******REMOVED******REMOVED***} catch (Exception e) {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***return ResponseEntity.status(500)
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***.body(new MessageResponse("Failed to add listener: " + e.getMessage()));
***REMOVED******REMOVED******REMOVED******REMOVED***}
***REMOVED******REMOVED***}

***REMOVED******REMOVED***@Operation(
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***summary = "Returns all team listeners"
***REMOVED******REMOVED***)
***REMOVED******REMOVED***@GetMapping("/{teamId}/listeners")
***REMOVED******REMOVED***public Set<String> teamListeners(@PathVariable String teamId) {
***REMOVED******REMOVED******REMOVED******REMOVED***return listenersByTeam.getOrDefault(teamId, Collections.emptySet());
***REMOVED******REMOVED***}

***REMOVED******REMOVED***private TelegramMessageDTO formatMessage(String teamId, TdApi.Message message) {
***REMOVED******REMOVED******REMOVED******REMOVED***String text = "";
***REMOVED******REMOVED******REMOVED******REMOVED***if (message.content instanceof TdApi.MessageText msgText) {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***text = msgText.text.text;
***REMOVED******REMOVED******REMOVED******REMOVED***}
***REMOVED******REMOVED******REMOVED******REMOVED***return new TelegramMessageDTO(teamId, message.chatId, message.date, text);
***REMOVED******REMOVED***}

}
