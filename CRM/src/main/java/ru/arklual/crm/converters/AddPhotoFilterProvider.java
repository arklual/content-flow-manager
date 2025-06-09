package ru.arklual.crm.converters;

import org.springframework.stereotype.Component;
import ru.arklual.crm.dto.AddPhotoFilter;
import ru.arklual.crm.dto.Filter;
import ru.arklual.crm.dto.protobuf.PostProto;

@Component
public class AddPhotoFilterProvider implements FilterProvider {

    @Override
    public boolean supportsGrpc(PostProto.Filter grpc) {
        return grpc.hasAddPhoto();
    }

    @Override
    public boolean supportsDto(Filter dto) {
        return dto.getAddPhoto() != null;
    }

    @Override
    public void getBuilderFromGrpc(Filter.FilterBuilder builder, PostProto.Filter grpc) {
        builder.addPhoto(AddPhotoFilter.builder()
                .trigger(grpc.getAddPhoto().getTrigger())
                .photoUrl(grpc.getAddPhoto().getPhotoUrl())
                .build());
    }

    @Override
    public void getBuilderFromDto(PostProto.Filter.Builder builder, Filter dto) {
        builder.setAddPhoto(PostProto.AddPhotoFilter.newBuilder()
                .setTrigger(dto.getAddPhoto().getTrigger())
                .setPhotoUrl(dto.getAddPhoto().getPhotoUrl())
                .build());
    }
}
