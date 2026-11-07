package com.github.br.libgdx.common.structure;

import com.badlogic.gdx.utils.GdxRuntimeException;

public class GameSettings {

    private final int progressBarWidth;
    private final int progressBarHeight;

    // virtual
    private final int virtualScreenWidth;
    private final int virtualScreenHeight;

    private final boolean isCenterCamera;

    // box2d
    private final float ppu; // pixel per unit
    private final float unitWidth;
    private final float unitHeight;

    private final float timeStep;
    private final int velocityIterations;
    private final int positionIterations;

    private final GamePlaySettings gamePlaySettings;

    private final boolean isUsingKtx2;
    private final boolean isActiveGlProfiler;
    private final boolean isActiveTiledRender;
    private final boolean isRenderDebugBox2d;

    private GameSettings(Builder builder) {
        this.progressBarWidth = builder.progressBarWidth;
        this.progressBarHeight = builder.progressBarHeight;


        this.virtualScreenWidth = builder.virtualScreenWidth;
        this.virtualScreenHeight = builder.virtualScreenHeight;
        this.isCenterCamera = builder.isCenterCamera;

        this.ppu = builder.ppu;
        this.unitWidth = this.virtualScreenWidth / this.ppu;
        this.unitHeight = this.virtualScreenHeight / this.ppu;

        this.timeStep = builder.timeStep;
        this.velocityIterations = builder.velocityIterations;
        this.positionIterations = builder.positionIterations;

        this.gamePlaySettings = builder.gamePlaySettings;
        this.isUsingKtx2 = builder.isUsingKtx2;
        this.isActiveTiledRender = builder.isActiveTiledRender;
        this.isRenderDebugBox2d = builder.isDebugBox2d;
        this.isActiveGlProfiler = builder.isActiveGlProfiler;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getProgressBarWidth() {
        return progressBarWidth;
    }

    public int getProgressBarHeight() {
        return progressBarHeight;
    }

    public int getVirtualScreenWidth() {
        return virtualScreenWidth;
    }

    public int getVirtualScreenHeight() {
        return virtualScreenHeight;
    }

    public float getPpu() {
        return ppu;
    }

    public float getUnitWidth() {
        return unitWidth;
    }

    public float getUnitHeight() {
        return unitHeight;
    }

    public float getTimeStep() {
        return timeStep;
    }

    public int getVelocityIterations() {
        return velocityIterations;
    }

    public int getPositionIterations() {
        return positionIterations;
    }

    public GamePlaySettings getGamePlaySettings() {
        return gamePlaySettings;
    }

    public boolean isCenterCamera() {
        return isCenterCamera;
    }

    public boolean isUsingKtx2() {
        return isUsingKtx2;
    }

    public boolean isActiveGlProfiler() {
        return isActiveGlProfiler;
    }

    public boolean isActiveTiledRender() {
        return isActiveTiledRender;
    }

    public boolean isRenderDebugBox2d() {
        return isRenderDebugBox2d;
    }

    public static class Builder {

        private int progressBarWidth = 100;
        private int progressBarHeight = 25;

        private int virtualScreenWidth = 1280;
        private int virtualScreenHeight = 720;

        private float ppu = 128f;

        private float timeStep = 1 / 60f;
        private int velocityIterations = 6;
        private int positionIterations = 2;

        private boolean isCenterCamera = false;

        private boolean isUsingKtx2 = false;
        private boolean isActiveGlProfiler = true;
        private boolean isActiveTiledRender = true;
        private boolean isDebugBox2d = true;

        private GamePlaySettings gamePlaySettings;

        public Builder setProgressBarWidth(int progressBarWidth) {
            this.progressBarWidth = progressBarWidth;
            return this;
        }

        public Builder setProgressBarHeight(int progressBarHeight) {
            this.progressBarHeight = progressBarHeight;
            return this;
        }

        public Builder setVirtualScreenWidth(int virtualScreenWidth) {
            this.virtualScreenWidth = virtualScreenWidth;
            return this;
        }

        public Builder setVirtualScreenHeight(int virtualScreenHeight) {
            this.virtualScreenHeight = virtualScreenHeight;
            return this;
        }

        public Builder setPpu(float ppu) {
            this.ppu = ppu;
            return this;
        }

        public Builder setTimeStep(float timeStep) {
            this.timeStep = timeStep;
            return this;
        }

        public Builder setVelocityIterations(int velocityIterations) {
            this.velocityIterations = velocityIterations;
            return this;
        }

        public Builder setPositionIterations(int positionIterations) {
            this.positionIterations = positionIterations;
            return this;
        }

        public Builder setGamePlaySettings(GamePlaySettings gamePlaySettings) {
            if (gamePlaySettings == null) {
                throw new GdxRuntimeException("'gamePlaySettings' must not be null");
            }
            this.gamePlaySettings = gamePlaySettings;
            return this;
        }

        public Builder setCenterCamera(boolean centerCamera) {
            this.isCenterCamera = centerCamera;
            return this;
        }

        public Builder setUsingKtx2(boolean usingKtx2) {
            this.isUsingKtx2 = usingKtx2;
            return this;
        }

        public Builder setActiveGlProfiler(boolean activeGlProfiler) {
            isActiveGlProfiler = activeGlProfiler;
            return this;
        }

        public Builder setActiveTiledRender(boolean activeTiledRender) {
            isActiveTiledRender = activeTiledRender;
            return this;
        }

        public Builder setDebugBox2d(boolean debugBox2d) {
            isDebugBox2d = debugBox2d;
            return this;
        }

        public GameSettings build() {
            return new GameSettings(this);
        }

    }

}
