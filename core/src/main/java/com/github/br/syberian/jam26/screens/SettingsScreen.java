package com.github.br.syberian.jam26.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.br.libgdx.common.structure.GameSettings;
import com.github.br.libgdx.common.structure.screen.AbstractGameScreen;
import com.github.br.syberian.jam26.Resources;

public class SettingsScreen extends AbstractGameScreen {

    private Texture background;

    protected OrthographicCamera backgroundCamera;
    protected Viewport backgroundViewport;

    private SpriteBatch spriteBatch;
    private Stage stage;

    private Skin gameSkin;


    @Override
    public void show() {
        AssetManager assetManager = getGameManager().assetManager;
        background = assetManager.get(Resources.MAIN_MENU_BACKGROUND, Texture.class);
        gameSkin = assetManager.get(Resources.SKIN);

        GameSettings gameSettings = getGameManager().gameSettings;

        // --- Инициализация вьюпорта для фона ---
        backgroundCamera = new OrthographicCamera();
        backgroundViewport = new ExtendViewport(
            gameSettings.getVirtualScreenWidth(),
            gameSettings.getVirtualScreenHeight(),
            backgroundCamera
        );
        backgroundViewport.apply(true);

        spriteBatch = new SpriteBatch();
        stage = new Stage(backgroundViewport, spriteBatch);


        TextButton backButton = createTextButton("НАЗАД", "title");
        backButton.setX(1920 - 500);
        backButton.setY(60);
        backButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getGameManager().screenStateManager.changeCurrentState(GameScreens.MENU);
            }
        });
        stage.addActor(backButton);

        Gdx.input.setInputProcessor(stage);
    }

    private Label createTitle(String text) {
        return new Label(text, gameSkin);
    }

    private TextButton createTextButton(String text, String style) {
        return new TextButton(text, gameSkin, style);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        GameSettings gameSettings = getGameManager().gameSettings;
        float virtualWidth = gameSettings.getVirtualScreenWidth();   // 1920
        float virtualHeight = gameSettings.getVirtualScreenHeight(); // 1080

        backgroundCamera.update();
        backgroundViewport.apply(true);
        spriteBatch.setProjectionMatrix(backgroundCamera.combined);

        spriteBatch.begin();

        // Рассчитываем центрирование по X: (1920 - 2560) / 2 = -320
        // По Y картинка (1080) идеально совпадает с виртуальной высотой (1080), поэтому 0
        float backgroundX = (virtualWidth - background.getWidth()) / 2f;
        float backgroundY = 0f;

        // Обязательно передаем ширину и высоту текстуры, чтобы LibGDX правильно ее масштабировал
        spriteBatch.draw(background, backgroundX, backgroundY, background.getWidth(), background.getHeight());

        spriteBatch.end();

        spriteBatch.setProjectionMatrix(backgroundCamera.combined);
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        backgroundViewport.update(width, height, false);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

}
