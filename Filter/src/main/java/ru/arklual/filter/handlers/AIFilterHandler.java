package ru.arklual.filter.handlers;

import chat.giga.client.GigaChatClient;
import chat.giga.client.auth.AuthClient;
import chat.giga.client.auth.AuthClientBuilder;
import chat.giga.model.ModelName;
import chat.giga.model.Scope;
import chat.giga.model.completion.ChatMessage;
import chat.giga.model.completion.ChatMessageRole;
import chat.giga.model.completion.CompletionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.arklual.filter.dto.protobuf.PostProto;

@Component
public class AIFilterHandler implements FilterHandler {

    @Value("${giga.chat.auth-key}")
    private String authKey;

    @Override
    public boolean supports(PostProto.Filter filter) {
        return filter.hasAiFilter();
    }

    @Override
    public PostProto.Post applyFilter(PostProto.Filter filter, PostProto.Post post) {
        String content = post.getContent();
        PostProto.AIFilter f = filter.getAiFilter();
        if (content.contains(f.getTrigger())) {
            GigaChatClient client = GigaChatClient.builder()
                    .verifySslCerts(false)
                    .authClient(AuthClient.builder()
                            .withOAuth(AuthClientBuilder.OAuthBuilder.builder()
                                    .scope(Scope.GIGACHAT_API_PERS)
                                    .authKey(authKey)
                                    .build())
                            .build())
                    .build();

            String result = client.completions(CompletionRequest.builder()
                            .model(ModelName.GIGA_CHAT)
                            .message(ChatMessage.builder()
                                    .content(f.getPrompt() + "\n\n-----\nисходный текст:\n" + content)
                                    .role(ChatMessageRole.USER)
                                    .build())
                            .build())
                    .choices().getFirst().message().content();

            return post.toBuilder()
                    .setContent(result)
                    .build();
        }
        return post;
    }
}
