package ru.arklual.telegramparser.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddPhotoFilterEntity implements FilterEntity {
    private final String type = "add_photo";
    private String trigger;
    private String photoUrl;
}