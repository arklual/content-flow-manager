package ru.arklual.crm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddPhotoFilter {
    private String trigger;
    private String photoUrl;
}
