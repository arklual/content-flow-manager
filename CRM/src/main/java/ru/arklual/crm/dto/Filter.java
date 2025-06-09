package ru.arklual.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Filter {
    private AddPhotoFilter addPhoto;
    private RemovePhotoFilter removePhoto;
    private TextReplaceFilter textReplace;
    private AIFilter aiFilter;
}
