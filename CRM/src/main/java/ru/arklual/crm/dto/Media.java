package ru.arklual.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.arklual.crm.dto.protobuf.PostProto;

@Data
@Builder
@AllArgsConstructor
public class Media {
    private String url;
    private PostProto.MediaType mediaType;
}
