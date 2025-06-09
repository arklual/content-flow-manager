package ru.arklual.telegramparser.registries;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileUploadFuturesRegistry {

    private final Map<Integer, CompletableFuture<String>> fileUploadFutures = new ConcurrentHashMap<>();

    public void registerFuture(Integer fileId, CompletableFuture<String> future) {
        fileUploadFutures.put(fileId, future);
    }


    public CompletableFuture<String> getFuture(Integer fileId) {
        return fileUploadFutures.get(fileId);
    }

    public void removeFuture(Integer fileId) {
        fileUploadFutures.remove(fileId);
    }


}
