package ru.arklual.filter.handlers;

import org.springframework.stereotype.Component;
import ru.arklual.filter.dto.protobuf.PostProto;

@Component
public class TextReplaceFilterHandler implements FilterHandler {

    @Override
    public boolean supports(PostProto.Filter filter) {
        return filter.hasTextReplace();
    }

    @Override
    public PostProto.Post applyFilter(PostProto.Filter filter, PostProto.Post post) {
        String content = post.getContent();
        PostProto.TextReplaceFilter f = filter.getTextReplace();
        if (content.contains(f.getTrigger())) {
            return post.toBuilder()
                    .setContent(content.replace(f.getPattern(), f.getReplacement()))
                    .build();
        }
        return post;
    }
}
