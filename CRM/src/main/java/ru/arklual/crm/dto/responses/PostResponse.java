package ru.arklual.crm.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.arklual.crm.dto.Filter;
import ru.arklual.crm.dto.Media;
import ru.arklual.crm.dto.Sink;
import ru.arklual.crm.dto.Source;
import ru.arklual.crm.dto.protobuf.PostProto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class PostResponse {

    private String id;
    private UUID teamId;
    private Source source;
    private String content;
    private Media media;
    private Instant createdAt;
    private Instant updatedAt;
    private PostProto.PostStatus status;
    private boolean requiresModeration;
    private boolean isCleaned;
    private List<Sink> sinks;
    private List<Filter> filters;

}
