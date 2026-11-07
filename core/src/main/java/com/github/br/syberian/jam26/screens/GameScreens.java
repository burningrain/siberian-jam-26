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

    GameScreenState PHYSICS_LEVEL_1_SCREEN = new GameScreenState(
        new PhysicsLevelScreen(
            Resources.Music.LEVEL_THEME,
            GameScreens.MENU,
            Resources.Tmx.PHYSICS_LEVEL_1,
            Resources.SKIN
        ), new PhysicsLevelScreenLoader(Resources.Music.LEVEL_THEME)
    );

    GameScreenState LEVEL_1 = new GameScreenState(
        new PhysicsLevelScreen(
            Resources.Music.MENU_THEME,
            GameScreens.PHYSICS_LEVEL_1_SCREEN,
            Resources.Tmx.LEVEL_1,
            Resources.SKIN
        ),
        new Level1AssetLoader(Resources.Music.MENU_THEME)
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
    GameScreenState GAME_SCREEN = new GameScreenState(
        new GameScreen(), new GameScreenAssetLoader()
    );

}
