package ru.arklual.filter.handlers;

import org.springframework.stereotype.Component;
import ru.arklual.filter.dto.protobuf.PostProto;

import java.util.ArrayList;
import java.util.List;

@Component
public class AddPhotoFilterHandler implements FilterHandler {
    @Override
    public boolean supports(PostProto.Filter filter) {
        return filter.hasAddPhoto();
    }

    @Override
    public PostProto.Post applyFilter(PostProto.Filter filter, PostProto.Post post) {
        String content = post.getContent();
        PostProto.AddPhotoFilter f = filter.getAddPhoto();
        if (content.contains(f.getTrigger())) {
            List<PostProto.Media> mediaList = new ArrayList<>(post.getMediaList());
            mediaList.add(PostProto.Media.newBuilder()
                    .setUrl(f.getPhotoUrl())
                    .setType(PostProto.MediaType.PHOTO)
                    .build());

            return post.toBuilder()
                    .clearMedia()
                    .addAllMedia(mediaList)
                    .build();
        }
        return post;
    }
}
