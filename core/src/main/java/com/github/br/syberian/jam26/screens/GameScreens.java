package com.github.br.syberian.jam26.screens;

import com.github.br.libgdx.common.structure.screen.statemachine.GameScreenState;
import com.github.br.syberian.jam26.Resources;

public interface GameScreens {

    // начальные уровни должны быть ниже!
    // а конечные выше!!!

    GameScreenState MENU = new GameScreenState(
        new MainScreen(), new MainAssetLoader()
    );

    GameScreenState SETTINGS = new GameScreenState(
        new SettingsScreen(), new SettingsScreenLoader()
    );

//    GameScreenState LEVEL_1 = new GameScreenState(
//        new Level1Screen(
//            GameScreens.MENU,
//            Resources.Tmx.LEVEL_1,
//            Resources.SKIN,
//            true,
//            true
//        ),
//        new Level1AssetLoader()
//    );

    GameScreenState PHYSICS_LEVEL_1_SCREEN = new GameScreenState(
        new PhysicsLevelScreen(
            GameScreens.MENU,
            Resources.Tmx.PHYSICS_LEVEL_1,
            Resources.SKIN,
            false,
            true
        ), new PhysicsLevelScreenLoader()
    );


//    GameScreenState TEST_ROOM = new GameScreenState(
//        new TestRoomScreen(
//            Resources.Tmx.TEST_ROOM,
//            Resources.SKIN,
//            true,
//            true
//        ),
//        new TestRoomAssetLoader()
//    );
//
//    GameScreenState GAME_SCREEN = new GameScreenState(
//        new GameScreen(), new GameScreenAssetLoader()
//    );

}
