package com.github.br.syberian.jam26.screens;

import com.github.br.libgdx.common.structure.screen.statemachine.GameScreenState;
import com.github.br.syberian.jam26.Resources;

public interface GameScreens {

    GameScreenState GAME_SCREEN = new GameScreenState(
        new GameScreen(), new GameScreenAssetLoader()
    );

    GameScreenState MENU = new GameScreenState(
        new MainScreen(), new MainAssetLoader()
    );

    GameScreenState SETTINGS = new GameScreenState(
        new SettingsScreen(), new SettingsScreenLoader()
    );

    GameScreenState LEVEL_1 = new GameScreenState(
        new Level1Screen(
            Resources.Tmx.LEVEL_1,
            Resources.SKIN,
            true,
            true
        ),
        new Level1AssetLoader()
    );

    GameScreenState TEST_ROOM = new GameScreenState(
        new TestRoomScreen(
            Resources.Tmx.TEST_ROOM,
            Resources.SKIN,
            true,
            true
        ),
        new TestRoomAssetLoader()
    );

}
