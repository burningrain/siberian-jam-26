package com.github.br.libgdx.common.structure;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public final class SimpleUtils {

    private final GameSettings gameSettings;

    public SimpleUtils(GameSettings gameSettings) {
        this.gameSettings = gameSettings;
    }

    public float convertPixelsToMetres(float pixels) {
        return pixels / gameSettings.getPpu();
    }

    public float convertMetresToPixels(float metres) {
        return metres * gameSettings.getPpu();
    }

    public void rotatePointToAngle(Vector2 mutableVector, float degreeAngle) {
        float sinT = MathUtils.sinDeg(degreeAngle);
        float cosT = MathUtils.cosDeg(degreeAngle);

        mutableVector.x = mutableVector.x * cosT - mutableVector.y * sinT;
        mutableVector.y = mutableVector.x * sinT + mutableVector.y * cosT;
    }

}
