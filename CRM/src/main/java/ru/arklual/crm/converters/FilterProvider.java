package ru.arklual.crm.converters;

import ru.arklual.crm.dto.Filter;
import ru.arklual.crm.dto.protobuf.PostProto;

public interface FilterProvider {
    boolean supportsGrpc(PostProto.Filter grpc);
    boolean supportsDto(Filter dto);
    void getBuilderFromGrpc(Filter.FilterBuilder builder, PostProto.Filter grpc);
    void getBuilderFromDto(PostProto.Filter.Builder builder, Filter dto);
}
