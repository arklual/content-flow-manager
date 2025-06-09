package ru.arklual.filter.services;

import org.springframework.stereotype.Service;
import ru.arklual.filter.dto.protobuf.PostProto;
import ru.arklual.filter.handlers.FilterHandler;

import java.util.List;

@Service
public class FilterApplier {

    private final List<FilterHandler> handlers;

    public FilterApplier(List<FilterHandler> handlers) {
        this.handlers = handlers;
    }

    public PostProto.Post applyFilter(PostProto.Filter filter, PostProto.Post post) {
        for (FilterHandler handler : handlers) {
            if (handler.supports(filter)) {
                return handler.applyFilter(filter, post);
            }
        }
        return post;
    }
}
