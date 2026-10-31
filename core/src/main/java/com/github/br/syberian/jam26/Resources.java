package com.github.br.syberian.jam26;

public interface Resources {

    String SKIN_ATLAS = "skin/export/export.atlas";
    String SKIN = "skin/export/export.json";

    interface Tmx {
        String MENU = "level-1.tmx";
        String LEVEL_1 = "level-1.tmx";

        String TEST_ROOM = "test-room.tmx";
    }

    interface Atlases {
        String GAME_OBJECTS = "atlases/test-room.atlas";
    }

    /**
     * это текстурные регионы нестатичных объектов аля GameObject.
     * Статичные же зашиваем в ktx2 и tiled, ибо FULL_HD
     */
    interface TextureRegion {
        String HERO = "hero";
        String BOX = "box";
        String BULLET = "bullet";
        String CAMERA = "camera";
        String DOOR = "door";
        String ENEMY = "enemy";
    }

}
