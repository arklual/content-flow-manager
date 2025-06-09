package ru.arklual.filter.handlers;

import org.springframework.stereotype.Component;
import ru.arklual.filter.dto.protobuf.PostProto;

@Component
public class RemovePhotoFilterHandler implements FilterHandler {

    @Override
    public boolean supports(PostProto.Filter filter) {
        return filter.hasRemovePhoto();
    }

    @Override
    public PostProto.Post applyFilter(PostProto.Filter filter, PostProto.Post post) {
        String content = post.getContent();
        PostProto.RemovePhotoFilter f = filter.getRemovePhoto();
        if (content.contains(f.getTrigger())) {
            return post.toBuilder()
                    .clearMedia()
                    .build();
        }
        return post;
    }
}
