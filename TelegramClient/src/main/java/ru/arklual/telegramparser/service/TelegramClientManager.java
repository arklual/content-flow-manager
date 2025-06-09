package ru.arklual.telegramparser.service;

import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import ru.arklual.telegramparser.converters.FlowConverter;
import ru.arklual.telegramparser.dto.protobuf.Flow;
import ru.arklual.telegramparser.dto.protobuf.PostProto;
import ru.arklual.telegramparser.entities.FlowEntity;
import ru.arklual.telegramparser.registries.FileUploadFuturesRegistry;
import ru.arklual.telegramparser.registries.ListenerRegistry;
import ru.arklual.telegramparser.registries.QrCodeRegistry;
import ru.arklual.telegramparser.registries.StatesRegistry;
import ru.arklual.telegramparser.repositories.FlowRepository;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Service
public class TelegramClientManager implements DisposableBean {

    private final SimpleTelegramClientFactory clientFactory;

    private final StatesRegistry statesRegistry;

    final Map<String, SimpleTelegramClient> clients = new ConcurrentHashMap<>();
    private final KafkaProducer kafkaProducer;
    private final MediaUploader mediaUploader;
    private final FlowRepository flowRepository;
    private final FlowConverter flowConverter;
    private final FileUploadFuturesRegistry uploadFuturesRegistry;

    private final ListenerRegistry listenerRegistry;

    public TelegramClientManager(SimpleTelegramClientFactory clientFactory, StatesRegistry statesRegistry, KafkaProducer kafkaProducer, MediaUploader mediaUploader, FlowRepository flowRepository, FlowConverter flowConverter, FileUploadFuturesRegistry uploadFuturesRegistry, ListenerRegistry listenerRegistry) {
        this.clientFactory = clientFactory;
        this.statesRegistry = statesRegistry;
        this.kafkaProducer = kafkaProducer;
        this.mediaUploader = mediaUploader;
        this.flowRepository = flowRepository;
        this.flowConverter = flowConverter;
        this.uploadFuturesRegistry = uploadFuturesRegistry;
        this.listenerRegistry = listenerRegistry;
    }

    public SimpleTelegramClient getClient(String teamId) {
        return clients.get(teamId);
    }

    public void stopClient(String teamId) {
        SimpleTelegramClient client = clients.remove(teamId);
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public List<String> getAllTeamIds() {
        return new ArrayList<>(clients.keySet());
    }

    @Override
    public void destroy() {
        log.info("Shutting down all Telegram clients...");
        clients.values().forEach(client -> {
            try {
                client.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
        clients.clear();
        log.info("All Telegram clients stopped.");
    }

    public void startClientViaQr(String teamId, TDLibSettings settings, QrCodeRegistry qrRegistry) {
        SimpleTelegramClientBuilder builder = clientFactory.builder(settings);
        builder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, update -> {
            TdApi.AuthorizationState state = update.authorizationState;
            statesRegistry.setState(teamId, state.getClass().getSimpleName().replace("AuthorizationState", ""));
            if (state instanceof TdApi.AuthorizationStateWaitOtherDeviceConfirmation) {
                String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) state).link;
                qrRegistry.setQrCode(teamId, link);
            }
        });
        SimpleTelegramClient client = builder.build(AuthenticationSupplier.qrCode());
        client.execute(new TdApi.SetLogVerbosityLevel(2));
        clients.put(teamId, client);
    }

    public void addListener(String channelId, String teamId) {
        if (!listenerRegistry.isListenerExist(teamId, channelId)) {
            listenerRegistry.addListener(teamId, channelId);
            SimpleTelegramClient client = clients.get(teamId);
            if (client == null) return;
            long targetChatId = Long.parseLong(channelId);
            client.addUpdateHandler(TdApi.UpdateNewMessage.class, update -> {
                TdApi.Message message = update.message;
                if (message.chatId == targetChatId) {
                    formatMessage(teamId, message, client).thenApply(
                            dtos -> {
                                dtos.forEach(dto -> kafkaProducer.sendMessage("row.content", dto));
                                return dtos;
                            }
                    );
                }
            });
            client.addUpdateHandler(TdApi.UpdateFile.class, update -> {
                log.debug(update.toString());
                TdApi.File file = update.file;
                int fileId = file.id;
                CompletableFuture<String> future = uploadFuturesRegistry.getFuture(fileId);
                if (future == null) {
                    return;
                }
                if (file.local != null && file.local.isDownloadingCompleted
                        && file.local.path != null && !file.local.path.isEmpty()) {
                    uploadFuturesRegistry.removeFuture(fileId);
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return mediaUploader.uploadToS3(file.local.path);
                        } catch (Exception e) {
                            throw new CompletionException(e);
                        }
                    }).whenComplete((s3Url, ex) -> {
                        if (ex != null) {
                            future.completeExceptionally(ex);
                        } else {
                            future.complete(s3Url);
                        }
                    });
                }

            });
            client.addUpdateHandler(TdApi.UpdateFileGenerationStart.class, up -> log.info(up.toString()));
            client.addUpdateHandler(TdApi.UpdateFileGenerationStop.class, up -> log.info(up.toString()));
        }
    }

    CompletableFuture<List<PostProto.Post>> formatMessage(
            String teamId,
            TdApi.Message message,
            SimpleTelegramClient client) {


        String text = "";
        if (message.content instanceof TdApi.MessageText msgText) {
            text = msgText.text.text;
        }


        List<CompletableFuture<PostProto.Media>> mediaFutures = new ArrayList<>();

        if (message.content instanceof TdApi.MessagePhoto msgPhoto) {
            if (msgPhoto.caption != null) {
                text = msgPhoto.caption.text;
            }


            List<TdApi.PhotoSize> sortedSizes = Arrays.stream(msgPhoto.photo.sizes)
                    .sorted(Comparator.comparingLong(s -> -s.photo.expectedSize))
                    .toList();

            if (!sortedSizes.isEmpty()) {
                TdApi.PhotoSize largestSize = sortedSizes.get(Math.min(sortedSizes.size()/2+1, sortedSizes.size()-1));
                TdApi.File file = largestSize.photo;
                if (file != null) {

                    CompletableFuture<PostProto.Media> mediaFuture =
                            resolveMediaUrlOrUploadAsync(file, client)
                                    .thenApply(s3Url -> {
                                        if (s3Url == null || "UNAVAILABLE".equals(s3Url)) {
                                            return null;
                                        }
                                        return PostProto.Media.newBuilder()
                                                .setUrl(s3Url)
                                                .setType(PostProto.MediaType.PHOTO)
                                                .build();
                                    })
                                    .exceptionally(ex -> {
                                        log.error("Не удалось загрузить файл в S3", ex);
                                        return null;
                                    });

                    mediaFutures.add(mediaFuture);
                }
            }
        }


        List<FlowEntity> flows = flowRepository.findAllByTeamIdAndSourceChatId(
                teamId,
                String.valueOf(message.chatId)
        );


        CompletableFuture<Void> allMediaDone = CompletableFuture
                .allOf(mediaFutures.toArray(new CompletableFuture[0]));

        final String txt = text;
        return allMediaDone.thenApply(ignored -> {

            List<PostProto.Media> mediaList = mediaFutures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .toList();


            List<PostProto.Post> result = new ArrayList<>();
            for (FlowEntity flowEntity : flows) {
                Flow f = flowConverter.toProto(flowEntity);

                PostProto.Post post = PostProto.Post.newBuilder()
                        .setId(UUID.randomUUID().toString())
                        .setTeamId(teamId)
                        .setContent(txt)
                        .setCreatedAt(message.date)
                        .setUpdatedAt(message.date)
                        .setStatus(PostProto.PostStatus.DRAFT)
                        .setRequiresModeration(f.getRequiresModeration())
                        .setIsCleaned(false)
                        .addAllMedia(mediaList)
                        .addAllFilter(f.getFiltersList())
                        .addAllSink(f.getSinksList())
                        .setSource(f.getSource())
                        .build();

                result.add(post);
            }
            return result;
        });
    }


    CompletableFuture<String> resolveMediaUrlOrUploadAsync(TdApi.File file, SimpleTelegramClient client) {
        int fileId = file.id;
        if (file.local != null && file.local.isDownloadingCompleted
                && file.local.path != null && !file.local.path.isEmpty()) {
            String localPath = file.local.path;
            return CompletableFuture.supplyAsync(() -> mediaUploader.uploadToS3(localPath));
        }
        CompletableFuture<String> existing = uploadFuturesRegistry.getFuture(fileId);
        if (existing != null) {
            return existing;
        }
        CompletableFuture<String> future = new CompletableFuture<>();
        uploadFuturesRegistry.registerFuture(fileId, future);
        TdApi.DownloadFile downloadRequest = new TdApi.DownloadFile(
                fileId,
                1,
                0,
                0,
                false
        );
        client.send(downloadRequest)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    uploadFuturesRegistry.removeFuture(fileId);
                    return null;
                });
        return future;
    }

}
