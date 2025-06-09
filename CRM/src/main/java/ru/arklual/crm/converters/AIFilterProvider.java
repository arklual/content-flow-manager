package ru.arklual.crm.converters;

import org.springframework.stereotype.Component;
import ru.arklual.crm.dto.AIFilter;
import ru.arklual.crm.dto.Filter;
import ru.arklual.crm.dto.protobuf.PostProto;

@Component
public class AIFilterProvider implements FilterProvider {

    @Override
    public boolean supportsGrpc(PostProto.Filter grpc) {
        return grpc.hasAiFilter();
    }

    @Override
    public boolean supportsDto(Filter dto) {
        return dto.getAiFilter() != null;
    }

    @Override
    public void getBuilderFromGrpc(Filter.FilterBuilder builder, PostProto.Filter grpc) {
        builder.aiFilter(AIFilter.builder()
                .trigger(grpc.getAiFilter().getTrigger())
                .prompt(grpc.getAiFilter().getPrompt())
                .build());
    }

    @Override
    public void getBuilderFromDto(PostProto.Filter.Builder builder, Filter dto) {
        builder.setAiFilter(PostProto.AIFilter.newBuilder()
                .setTrigger(dto.getAiFilter().getTrigger())
                .setPrompt(dto.getAiFilter().getPrompt())
                .build());
    }
}
