package ru.arklual.telegramparser.converters;

public interface Converter<P, E> {
    String getKey();

    E toEntity(P proto);

    P toProto(E entity);
}
