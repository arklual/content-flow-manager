package ru.arklual.crm.converters;

import org.springframework.stereotype.Component;
import ru.arklual.crm.dto.*;
import ru.arklual.crm.dto.protobuf.PostProto;

import java.util.List;

@Component
public class FilterConverter {

    private final List<FilterProvider> providers;

    public FilterConverter(List<FilterProvider> providers) {
        this.providers = providers;
    }

    public Filter fromGrpc(PostProto.Filter grpc) {
        Filter.FilterBuilder builder = Filter.builder();

        providers.stream()
                .filter(provider -> provider.supportsGrpc(grpc))
                .findFirst()
                .ifPresent(provider -> provider.getBuilderFromGrpc(builder, grpc));

        return builder.build();
    }

    public PostProto.Filter toGrpc(Filter dto) {
        PostProto.Filter.Builder builder = PostProto.Filter.newBuilder();

        providers.stream()
                .filter(provider -> provider.supportsDto(dto))
                .findFirst()
                .ifPresent(provider -> provider.getBuilderFromDto(builder, dto));

        return builder.build();
    }
}
