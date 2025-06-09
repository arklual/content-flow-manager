package ru.arklual.telegramparser.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RemovePhotoFilterEntity implements FilterEntity {
    private final String type = "remove_photo";
    private String trigger;
}