package com.github.br.syberian.jam26.ecs.systems.physics;

public interface CollisionType {

    short CATEGORY_DEFAULT = 0x0001; // Стены, закрытые двери, ящики, игрок
    short CATEGORY_SENSOR  = 0x0002; // Триггеры, сенсоры, ОТКРЫТЫЕ двери

}
