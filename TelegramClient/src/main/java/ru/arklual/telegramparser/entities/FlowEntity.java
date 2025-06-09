package ru.arklual.telegramparser.entities;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "flows")
@Getter
@Setter
public class FlowEntity {
    @Id
    private ObjectId id;

    private String teamId;

    private SourceEntity source;
    private List<SinkEntity> sinks;
    private List<FilterEntity> filters;

    private boolean requiresModeration;

    @LastModifiedDate
    private Instant updatedAt;


}
