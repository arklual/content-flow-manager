package ru.arklual.telegramparser.utils;

import it.tdlight.jni.TdApi;
import lombok.extern.slf4j.Slf4j;
import ru.arklual.telegramparser.dto.protobuf.PostProto;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi.SendMessage;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Slf4j
public class TelegramPoster {
    private final SimpleTelegramClient client;

    public TelegramPoster(SimpleTelegramClient client) {
        this.client = client;
    }

    public void sendPost(PostProto.Post post) {
        try {
            String chatId = extractChatId(post);
            if (chatId == null) {
                return;
            }
            String content = post.getContent();
            List<PostProto.Media> mediaItems = post.getMediaList();
            long chatIdLong = Long.parseLong(chatId);

            if (mediaItems.isEmpty()) {
                sendTextMessage(chatIdLong, content);
            } else{
                sendSingleMedia(chatIdLong, mediaItems.getFirst(), content);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String extractChatId(PostProto.Post post) {
        for (PostProto.Sink sink : post.getSinkList()) {
            if (sink.hasTelegramSink()) {
                return sink.getTelegramSink().getChatId();
            }
        }
        return null;
    }

    private void sendSingleMedia(long chatId, PostProto.Media media, String caption) throws IOException {
        Path tmp = Files.createTempFile("tg_", ".jpg");
        try (InputStream in = URI.create(media.getUrl()).toURL().openStream()) {
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
        }
        TdApi.InputFile file = new TdApi.InputFileLocal(tmp.toString());

        TdApi.FormattedText captionFmt = (caption == null || caption.isBlank())
                ? null
                : new TdApi.FormattedText(caption, new TdApi.TextEntity[0]);

        TdApi.InputMessagePhoto photo = new TdApi.InputMessagePhoto(
                file, null, new int[0], 0, 0, captionFmt, null, false
        );

        client.send(new TdApi.SendMessage(chatId, 0, null, null, null, photo))
                .exceptionally(ex -> {
                    log.error("send photo", ex);
                    return null;
                }).whenComplete(
                        (result, throwable) -> tmp.toFile().deleteOnExit()
                );
    }

    private void sendTextMessage(long chatId, String text) {
        if (text == null || text.isBlank()) return;

        TdApi.FormattedText body = new TdApi.FormattedText(text, new TdApi.TextEntity[0]);

        TdApi.InputMessageText msg = new TdApi.InputMessageText(
                body,
                null,
                false
        );

        SendMessage textReq = new SendMessage(
                chatId,
                0L,
                null,
                null,
                null,
                msg
        );

        client.send(textReq)
                .exceptionally(ex -> {
                    log.error("sendText failed", ex);
                    return null;
                });
    }
}
