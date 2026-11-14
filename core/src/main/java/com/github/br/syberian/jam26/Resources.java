package com.github.br.syberian.jam26;

public interface Resources {

    String SKIN_ATLAS = "skin/export/export.atlas";
    String SKIN = "skin/export/export.json";

    String MAIN_MENU_BACKGROUND = "pictures/post-2-zero.png";

    interface Music {
        String LEVEL_THEME = "music/game_mus.mp3";
        String MENU_THEME = "music/menu_mus.mp3";
    }

    interface Sound {
        String OPEN_METAL_DOOR = "sounds/open_metal_door.wav";
        String CLOSE_METAL_DOOR = "sounds/close_metal_door.wav";

        String TALK = "sounds/talk.wav";
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
        String BED_GREEN = "bed";
        String BED_GRAY = "bed-2";
        String BLIN = "blin";
        String CAB = "cab";
        String CAM_1 = "cam1";
        String CAM_2 = "cam2";
        String CAM_3 = "cam-3";
        String CHA = "cha";
        String CHA_2 = "cha-2";
        String HERO = "char1";
        String DOOR_GRAY = "door1";
        String DOOR_BROWN = "door2";
        String KEY = "key1";
        String KEY_ITM = "key-itm";
        String MED = "med1";
        String PUMP = "pump";
        String SAN = "san";
        String SAN_2 = "san-2";
        String TABL = "tabl";
        String TABLE_COMP = "table-comp";
        String TRA_1 = "tra-1";
        String TRA_2 = "tra-2";
        String COOLER = "wat";
        String CUP = "wat-2";
        String BOX_OPEN = "korob-open";
        String SECURITY = "secur1";
        String LEVER = "richag1";
        String IRON_CABINET = "skaf_zelezo1";
        String CART = "telezka1";
        String BOX_CLOSED = "korob";
        String WIRES = "cab";
        String WIRE_CUTTERS = "kysa4-m";
        String TOILET = "san";
        String SINK = "san-2";
        String TABLE = "tabl";
        String CHAIR = "cha-2";
        String SPORT_BENCH = "tra-1";
        String FID_SPORT_BENCH = "tra-2";
        String DINING_BENCH = "stol-styl";
        String SECURITY_MONITORS = "table-comp";
        String GATE = "vorota";
        String PUMPKIN = "pump";
        String BARBELL = "shtanga";
        String WEIGHT_PLATE = "blin";
        String BOX_HOLDER = "stelaz-korob";
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
