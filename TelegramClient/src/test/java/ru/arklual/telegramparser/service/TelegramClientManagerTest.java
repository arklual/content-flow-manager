package ru.arklual.telegramparser.service;

import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.arklual.telegramparser.converters.FlowConverter;
import ru.arklual.telegramparser.dto.protobuf.Flow;
import ru.arklual.telegramparser.dto.protobuf.PostProto;
import ru.arklual.telegramparser.entities.FlowEntity;
import ru.arklual.telegramparser.registries.*;
import ru.arklual.telegramparser.repositories.FlowRepository;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TelegramClientManagerTest {

    @Mock
    SimpleTelegramClientFactory clientFactory;
    @Mock
    SimpleTelegramClientBuilder clientBuilder;
    @Mock
    SimpleTelegramClient client;

    @Mock
    StatesRegistry statesRegistry;
    @Mock
    MediaUploader mediaUploader;
    @Mock
    FlowRepository flowRepo;
    @Mock
    FlowConverter flowConv;
    @Mock
    FileUploadFuturesRegistry uploadReg;
    @Mock
    ListenerRegistry listenerReg;
    @Mock
    QrCodeRegistry qrReg;


    @InjectMocks
    TelegramClientManager mgr;

    @Captor
    ArgumentCaptor<TdApi.Function<?>> tdFuncCap;

    private Object authHandler;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);

        when(clientFactory.builder(any())).thenReturn(clientBuilder);
        when(clientBuilder.build(any())).thenReturn(client);
        when(client.send(any())).thenReturn(CompletableFuture.completedFuture(null));

        doAnswer(inv -> {
            authHandler = inv.getArgument(1);
            return clientBuilder;
        }).when(clientBuilder)
                .addUpdateHandler(eq(TdApi.UpdateAuthorizationState.class), any());
    }

    @Test
    void startClientViaQr_storesAndHandlesQR() throws Exception {
        mgr.startClientViaQr("team", mock(TDLibSettings.class), qrReg);

        assertSame(client, mgr.getClient("team"));
        assertNotNull(authHandler);

        var update = new TdApi.UpdateAuthorizationState(
                new TdApi.AuthorizationStateWaitOtherDeviceConfirmation("qr"));

        Method m = authHandler.getClass().getMethods()[0];
        m.invoke(authHandler, update);

        verify(statesRegistry).setState("team", "WaitOtherDeviceConfirmation");
        verify(qrReg).setQrCode("team", "qr");
    }

    @Test
    void resolve_localReady() {
        TdApi.File f = new TdApi.File();
        f.id = 1;
        f.local = new TdApi.LocalFile();
        f.local.path = "/tmp/a.jpg";
        f.local.isDownloadingCompleted = true;

        when(mediaUploader.uploadToS3("/tmp/a.jpg")).thenReturn("url");

        assertEquals("url", mgr.resolveMediaUrlOrUploadAsync(f, client).join());
    }

    @Test
    void resolve_existingFuture() {
        TdApi.File f = new TdApi.File();
        f.id = 2;
        CompletableFuture<String> cached = CompletableFuture.completedFuture("ok");
        when(uploadReg.getFuture(2)).thenReturn(cached);

        assertSame(cached, mgr.resolveMediaUrlOrUploadAsync(f, client));
    }

    @Test
    void resolve_registerAndSend() {
        TdApi.File f = new TdApi.File();
        f.id = 3;
        when(uploadReg.getFuture(3)).thenReturn(null);

        mgr.resolveMediaUrlOrUploadAsync(f, client);

        verify(uploadReg).registerFuture(eq(3), any());
        verify(client).send(tdFuncCap.capture());
        assertInstanceOf(TdApi.DownloadFile.class, tdFuncCap.getValue());
        assertEquals(3, ((TdApi.DownloadFile) tdFuncCap.getValue()).fileId);
    }

    @Test
    void addListener_registersOnce() {
        String team = "t", chat = "100";
        mgr.clients.put(team, client);

        when(listenerReg.isListenerExist(team, chat)).thenReturn(false).thenReturn(true);

        FlowEntity fe = new FlowEntity();
        fe.setId(new ObjectId());
        fe.setTeamId(team);
        when(flowRepo.findAllByTeamIdAndSourceChatId(team, chat)).thenReturn(List.of(fe));
        when(flowConv.toProto(fe)).thenReturn(
                Flow.newBuilder()
                        .setTeamId(team)
                        .setSource(PostProto.Source.newBuilder()
                                .setTelegramSource(
                                        ru.arklual.telegramparser.dto.protobuf.PostProto.TelegramSource
                                                .newBuilder().setChatId(chat).build())
                                .build())
                        .build());

        mgr.addListener(chat, team);

        verify(client).addUpdateHandler(eq(TdApi.UpdateNewMessage.class), any());
        verify(client).addUpdateHandler(eq(TdApi.UpdateFile.class), any());

        mgr.addListener(chat, team);
    }

    @Test
    void formatMessage_shouldBuildPost_withTextOnly() {
        String teamId = "team";
        long chatId = 12345L;

        TdApi.MessageText messageText = new TdApi.MessageText();
        messageText.text = new TdApi.FormattedText("Hello world", new TdApi.TextEntity[0]);

        TdApi.Message message = new TdApi.Message();
        message.content = messageText;
        message.chatId = chatId;
        message.date = 12345678;

        FlowEntity flowEntity = new FlowEntity();
        flowEntity.setId(new ObjectId());
        flowEntity.setTeamId(teamId);
        flowEntity.setRequiresModeration(true);

        when(flowRepo.findAllByTeamIdAndSourceChatId(teamId, String.valueOf(chatId)))
                .thenReturn(List.of(flowEntity));

        Flow proto = Flow.newBuilder()
                .setTeamId(teamId)
                .setRequiresModeration(true)
                .setSource(PostProto.Source.newBuilder()
                        .setTelegramSource(PostProto.TelegramSource.newBuilder()
                                .setChatId(String.valueOf(chatId)).build())
                        .build())
                .build();

        when(flowConv.toProto(flowEntity)).thenReturn(proto);

        List<PostProto.Post> result = mgr.formatMessage(teamId, message, client).join();

        assertEquals(1, result.size());
        PostProto.Post post = result.getFirst();
        assertEquals("Hello world", post.getContent());
        assertEquals(teamId, post.getTeamId());
        assertEquals(PostProto.PostStatus.DRAFT, post.getStatus());
        assertEquals(12345678, post.getCreatedAt());
        assertTrue(post.getRequiresModeration());
    }

    @Test
    void formatMessage_shouldProducePost_withPhoto() {
        String teamId = "team42";
        long chatId = 999999;

        TdApi.PhotoSize small = new TdApi.PhotoSize();
        small.photo = new TdApi.File();
        small.photo.id = 1;
        small.photo.expectedSize = 100;

        TdApi.PhotoSize big = new TdApi.PhotoSize();
        big.photo = new TdApi.File();
        big.photo.id = 2;
        big.photo.expectedSize = 10000;

        small.photo.local = new TdApi.LocalFile();
        small.photo.local.path = "/tmp/img_small.jpg";
        small.photo.local.isDownloadingCompleted = true;

        TdApi.MessagePhoto content = new TdApi.MessagePhoto();
        content.caption = new TdApi.FormattedText("Photo caption", new TdApi.TextEntity[0]);
        content.photo = new TdApi.Photo();
        content.photo.sizes = new TdApi.PhotoSize[]{small, big};

        TdApi.Message message = new TdApi.Message();
        message.chatId = chatId;
        message.date = 123456;
        message.content = content;

        FlowEntity entity = new FlowEntity();
        entity.setId(new ObjectId());
        entity.setTeamId(teamId);
        entity.setRequiresModeration(false);

        when(flowRepo.findAllByTeamIdAndSourceChatId(teamId, String.valueOf(chatId)))
                .thenReturn(List.of(entity));

        Flow flow = Flow.newBuilder()
                .setTeamId(teamId)
                .setRequiresModeration(false)
                .setSource(PostProto.Source.newBuilder()
                        .setTelegramSource(PostProto.TelegramSource.newBuilder().setChatId(String.valueOf(chatId))).build())
                .addFilters(PostProto.Filter.newBuilder()
                        .setTextReplace(PostProto.TextReplaceFilter.newBuilder()
                                .setTrigger("a").setPattern("b").setReplacement("c")))
                .addSinks(PostProto.Sink.newBuilder()
                        .setTelegramSink(PostProto.TelegramSink.newBuilder().setChatId("xyz")))
                .build();

        when(flowConv.toProto(entity)).thenReturn(flow);

        when(mediaUploader.uploadToS3("/tmp/img_small.jpg"))
                .thenReturn("https://s3.com/pic_small.jpg");

        when(client.send(any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        var posts = mgr.formatMessage(teamId, message, client).join();

        assertEquals(1, posts.size());
        PostProto.Post post = posts.getFirst();
        assertEquals("Photo caption", post.getContent());
        assertEquals(teamId, post.getTeamId());
        assertEquals(PostProto.PostStatus.DRAFT, post.getStatus());
        assertFalse(post.getRequiresModeration());

        assertEquals(1, post.getMediaList().size());
        var media = post.getMedia(0);
        assertEquals(PostProto.MediaType.PHOTO, media.getType());
        assertEquals("https://s3.com/pic_small.jpg", media.getUrl());

        assertEquals(1, post.getFilterCount());
        assertEquals(1, post.getSinkCount());
    }


}
