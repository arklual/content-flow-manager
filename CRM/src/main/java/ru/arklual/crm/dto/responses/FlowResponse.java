package ru.arklual.crm.dto.responses;

import lombok.Builder;
import lombok.Data;
import ru.arklual.crm.dto.Filter;
import ru.arklual.crm.dto.Sink;
import ru.arklual.crm.dto.Source;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class FlowResponse {

    private String id;

    private UUID teamId;

    private Source source;

    private List<Sink> sinks;

    private List<Filter> filters;

    private boolean requiresModeration;

    private Instant updatedAt;

}
