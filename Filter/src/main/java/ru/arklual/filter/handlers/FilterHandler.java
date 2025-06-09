package ru.arklual.filter.handlers;

import ru.arklual.filter.dto.protobuf.PostProto;

public interface FilterHandler {
    boolean supports(PostProto.Filter filter);
    PostProto.Post applyFilter(PostProto.Filter filter, PostProto.Post post);
}
