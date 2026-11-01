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
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
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

    private Slider musicSlider;
    private Slider soundSlider;


    @Override
    public void show() {
        AssetManager assetManager = getGameManager().assetManager;
        background = assetManager.get(Resources.MAIN_MENU_BACKGROUND, Texture.class);
        gameSkin = assetManager.get(Resources.SKIN);

        GameSettings gameSettings = getGameManager().gameSettings;

        // --- Инициализация вьюпорта для фона ---
        backgroundCamera = new OrthographicCamera();
        backgroundViewport = new FitViewport(
            gameSettings.getVirtualScreenWidth(),
            gameSettings.getVirtualScreenHeight(),
            backgroundCamera
        );
        backgroundViewport.apply(true);

        spriteBatch = new SpriteBatch();
        stage = new Stage(backgroundViewport, spriteBatch);

        TextButton okButton = createTextButton("OK", "title");
        okButton.setX(1000);
        okButton.setY(50);
        okButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float musicValue = musicSlider.getValue();
                float soundValue = soundSlider.getValue();
                getGameManager().audioSettings.setMusicVolume(musicValue);
                getGameManager().audioSettings.setSoundVolume(soundValue);

                getGameManager().screenStateManager.changeCurrentState(GameScreens.MENU);
            }
        });
        stage.addActor(okButton);

        TextButton backButton = createTextButton("НАЗАД", "title");
        backButton.setX(1300);
        backButton.setY(50);
        backButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getGameManager().screenStateManager.changeCurrentState(GameScreens.MENU);
            }
        });
        stage.addActor(backButton);

        int x = 150;
        Label musicLabel = createLabel("музыка");
        musicLabel.setX(x + 100f);
        musicLabel.setY(1080 - 150);
        stage.addActor(musicLabel);

        musicSlider = new Slider(0f, 1f, 0.05f, false, gameSkin);
        float preferredHeight = musicSlider.getPrefHeight();
        musicSlider.setSize(350, preferredHeight);
        musicSlider.setValue(1f);

        musicSlider.setX(x + 500f);
        musicSlider.setY(1080 - 150);
        stage.addActor(musicSlider);

        Label soundLabel = createLabel("звук");
        soundLabel.setX(x + 100f);
        soundLabel.setY(1080 - 250);
        stage.addActor(soundLabel);

        soundSlider = new Slider(0f, 1f, 0.05f, false, gameSkin);
        float soundPreferredHeight = soundSlider.getPrefHeight();
        soundSlider.setSize(350, soundPreferredHeight);
        soundSlider.setValue(1f);

        soundSlider.setX(x + 500f);
        soundSlider.setY(1080 - 250);
        stage.addActor(soundSlider);

        Gdx.input.setInputProcessor(stage);
    }

    private Label createLabel(String text) {
        return new Label(text, gameSkin);
    }

    private TextButton createTextButton(String text, String style) {
        return new TextButton(text, gameSkin, style);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1. Применяем вьюпорт (камера обновится сама благодаря корректному resize)
        backgroundViewport.apply();
        spriteBatch.setProjectionMatrix(backgroundCamera.combined);

        spriteBatch.begin();
        // Так как камера теперь отцентрирована по умолчанию (из-за true в resize),
        // начало координат (0,0) находится в левом нижнем углу ВИДИМОЙ области.
        // Рисуем картинку от нуля до её реальных размеров, либо используем размеры мира:
        spriteBatch.draw(background, 0, 0, backgroundViewport.getWorldWidth(), backgroundViewport.getWorldHeight());
        spriteBatch.end();

        spriteBatch.setProjectionMatrix(backgroundCamera.combined);
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        backgroundViewport.update(width, height, true);
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
