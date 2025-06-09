package ru.arklual.crm.converters;

import org.springframework.stereotype.Component;
import ru.arklual.crm.dto.Filter;
import ru.arklual.crm.dto.RemovePhotoFilter;
import ru.arklual.crm.dto.protobuf.PostProto;

@Component
public class RemovePhotoFilterProvider implements FilterProvider {

    @Override
    public boolean supportsGrpc(PostProto.Filter grpc) {
        return grpc.hasRemovePhoto();
    }

    @Override
    public boolean supportsDto(Filter dto) {
        return dto.getRemovePhoto() != null;
    }

    @Override
    public void getBuilderFromGrpc(Filter.FilterBuilder builder, PostProto.Filter grpc) {
        builder.removePhoto(RemovePhotoFilter.builder()
                .trigger(grpc.getRemovePhoto().getTrigger())
                .build());
    }

    @Override
    public void getBuilderFromDto(PostProto.Filter.Builder builder, Filter dto) {
        builder.setRemovePhoto(PostProto.RemovePhotoFilter.newBuilder()
                .setTrigger(dto.getRemovePhoto().getTrigger())
                .build());
    }
}
