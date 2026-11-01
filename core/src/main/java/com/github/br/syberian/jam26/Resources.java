package com.github.br.syberian.jam26;

public interface Resources {

    String SKIN_ATLAS = "skin/export/export.atlas";
    String SKIN = "skin/export/export.json";

    String MAIN_MENU_BACKGROUND = "pictures/post-2-zero.png";

    interface Music {
        String LEVEL_THEME = "music/game_mus.mp3";
        String MENU_THEME = "music/menu_mus.mp3";
    }

    interface SoundFx {

    }

    interface Tmx {
        String MENU = "menu.tmx";
        String LEVEL_1 = "level-1.tmx";

        String TEST_ROOM = "test-room.tmx";

        String PHYSICS_LEVEL_1 = "physics-level-1.tmx";
    }

    interface Atlases {
        String TEST_GAME_OBJECTS = "atlases/test-room.atlas";
        String GAME_OBJECTS = "atlases/game-objects.atlas";
    }


    interface TextureRegion {
        String BED = "bed";
        String BED2 = "bed-2";
        String BLIN = "blin";
        String CAB = "cab";
        String CAM_1 = "cam-1";
        String CAM_2 = "cam-2";
        String CAM_3 = "cam-3";
        String CHA = "cha";
        String CHA_2 = "cha-2";
        String CHAR = "char";
        String CHAR_1 = "char-1";
        String CHAR_2 = "char-2";
        String DOOR_1 = "door-1";
        String DOOR_2 = "door-2";
        String DOOR_3 = "door-3";
        String DOOR_4 = "door-4";
        String KEY = "key";
        String KEY_2 = "key-2";
        String KEY_ITM = "key-itm";
        String MED_1 = "med-1";
        String MED_2 = "med-2";
        String PUMP = "pump";
        String SAN = "san";
        String ENEMY = "san-2";
        String SAN_2 = "san-2";
        String TABL = "tabl";
        String TABLE_COMP = "table-comp";
        String TRA_1 = "tra-1";
        String TRA_2 = "tra-2";
        String WAT = "wat";
        String WAT_2 = "wat2";
        String BOX = "korob-open";
    }



    /**
     * это текстурные регионы нестатичных объектов аля GameObject.
     * Статичные же зашиваем в ktx2 и tiled, ибо FULL_HD
     */
    interface TestTextureRegion {
        String HERO = "hero";
        String BOX = "box";
        String BULLET = "bullet";
        String CAMERA = "camera";
        String DOOR = "door";
        String ENEMY = "enemy";
    }

}
