package ru.arklual.crm.converters;

import org.springframework.stereotype.Component;
import ru.arklual.crm.dto.Filter;
import ru.arklual.crm.dto.TextReplaceFilter;
import ru.arklual.crm.dto.protobuf.PostProto;

@Component
public class TextReplaceFilterProvider implements FilterProvider {

    @Override
    public boolean supportsGrpc(PostProto.Filter grpc) {
        return grpc.hasTextReplace();
    }

    @Override
    public boolean supportsDto(Filter dto) {
        return dto.getTextReplace() != null;
    }

    @Override
    public void getBuilderFromGrpc(Filter.FilterBuilder builder, PostProto.Filter grpc) {
        builder.textReplace(TextReplaceFilter.builder()
                .trigger(grpc.getTextReplace().getTrigger())
                .pattern(grpc.getTextReplace().getPattern())
                .replacement(grpc.getTextReplace().getReplacement())
                .build());
    }

    @Override
    public void getBuilderFromDto(PostProto.Filter.Builder builder, Filter dto) {
        builder.setTextReplace(PostProto.TextReplaceFilter.newBuilder()
                .setTrigger(dto.getTextReplace().getTrigger())
                .setPattern(dto.getTextReplace().getPattern())
                .setReplacement(dto.getTextReplace().getReplacement())
                .build());
    }
}
