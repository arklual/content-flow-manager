package ru.arklual.crm.dto.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.arklual.crm.dto.Filter;
import ru.arklual.crm.dto.Sink;
import ru.arklual.crm.dto.Source;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class FlowRequest {

    @NotNull
    private UUID teamId;

    @NotNull
    private Source source;

    @NotNull
    @Size(min = 1)
    private List<Sink> sinks;

    @NotNull
    private boolean requiresModeration;

    @NotNull
    private List<Filter> filters;
}
