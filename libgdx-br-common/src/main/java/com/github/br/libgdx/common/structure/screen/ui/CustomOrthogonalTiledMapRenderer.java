package com.github.br.libgdx.common.structure.screen.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapGroupLayer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.github.br.libgdx.common.structure.screen.ui.vfx.VfxEffectPooledFactory;

public class CustomOrthogonalTiledMapRenderer extends OrthogonalTiledMapRenderer {

    public static final String IS_CUSTOM_LAYER_RENDERER = "is_custom_layer_renderer";
    public static final String IS_UI_LAYER = "is_ui_layer";
    public static final String IS_OBJECT_LAYER = "is_object_layer";
    public static final String IS_VFX_EFFECT_ENABLED = "is_vfx_effect_enabled";
    public static final String VFX_LAYER_EFFECT = "vfx_layer_effect";

    private final Matrix4 tmpMatrix = new Matrix4();

    private final ObjectMap<String, Stage> stages = new ObjectMap<>();
    private final Viewport viewport;
    private final ActorFactory actorFactory;
    private final InputMultiplexer inputMultiplexer;

    private final VfxEffectPooledFactory vfxEffectPooledFactory;
    private final CustomLayerRenderer customLayerRenderer;
    private VfxManager vfxManager;
    private boolean isVfxEnabled;

    public CustomOrthogonalTiledMapRenderer(
        CustomLayerRenderer customLayerRenderer,
        ActorFactory actorFactory,
        Viewport viewport,
        TiledMap map,
        float unitScale
    ) {
        this(customLayerRenderer, actorFactory, viewport, map, unitScale, false, null, 0, 0);
    }

    public CustomOrthogonalTiledMapRenderer(
        CustomLayerRenderer customLayerRenderer,
        ActorFactory actorFactory,
        Viewport viewport,
        TiledMap map,
        float unitScale,
        boolean isVfxEnabled,
        VfxEffectPooledFactory vfxEffectPooledFactory,
        int bufferWidth,
        int bufferHeight
    ) {
        super(map, unitScale);
        this.customLayerRenderer = customLayerRenderer;
        this.isVfxEnabled = isVfxEnabled;
        this.vfxEffectPooledFactory = vfxEffectPooledFactory;
        this.viewport = viewport;
        this.actorFactory = actorFactory;

        // vfx
        if (isVfxEnabled) {
            vfxManager = new VfxManager(Pixmap.Format.RGBA8888, bufferWidth, bufferHeight);
        }

        clampToEdgeTextures(map);
        inputMultiplexer = new InputMultiplexer();
        createStageActors(inputMultiplexer, map.getLayers());
    }

    @Override
    public void renderMapLayer(MapLayer layer) {
        MapProperties properties = layer.getProperties();
        Boolean isVfxLayerEnabled = properties.get(IS_VFX_EFFECT_ENABLED, Boolean.class);
        String vfxEffect = properties.get(VFX_LAYER_EFFECT, String.class);
        boolean isHasEffects = false;
        if (isVfxEnabled && isVfxLayerEnabled != null && isVfxLayerEnabled) {
            Array<ChainVfxEffect> effects = vfxEffectPooledFactory.getEffects(vfxEffect);
            isHasEffects = effects != null && effects.size > 0;
            if (isHasEffects) {
                Batch batch = getBatch();
                if (batch.isDrawing()) {
                    batch.end();
                }

                vfxManager.removeAllEffects();
                for (ChainVfxEffect effect : effects) {
                    vfxManager.addEffect(effect);
                }
                vfxEffectPooledFactory.update(Gdx.graphics.getDeltaTime(), vfxEffect);

                vfxManager.cleanUpBuffers(Color.CLEAR_WHITE);
                vfxManager.beginInputCapture();
                batch.begin();
            }
        }

        super.renderMapLayer(layer);

        if (isVfxEnabled && isVfxLayerEnabled != null && isVfxLayerEnabled && isHasEffects) {
            batch.end();

            vfxManager.endInputCapture();
            vfxManager.applyEffects();

            Texture fboTexture = vfxManager.getResultBuffer().getTexture();
            fboTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

            vfxManager.renderToScreen();

            batch.begin();
        }
    }

    @Override
    public void renderObjects(MapLayer layer) {
        MapProperties properties = layer.getProperties();
        Boolean isCustomLayerRenderer = properties.get(IS_CUSTOM_LAYER_RENDERER, Boolean.class);
        if (isCustomLayerRenderer != null && isCustomLayerRenderer) {
            renderLayerWithCustomRenderer(layer);
        } else {
            renderObjectLayer(layer);
        }
    }

    protected void renderLayerWithCustomRenderer(MapLayer layer) {
        if (this.customLayerRenderer == null) {
            throw new GdxRuntimeException("You need set customLayerRenderer!");
        }

        boolean wasDrawing = batch.isDrawing();
        if (wasDrawing) {
            batch.end();
        }

        this.customLayerRenderer.render(layer, viewport);

        if (wasDrawing) {
            batch.begin();
        }
    }

    private void renderObjectLayer(MapLayer layer) {
        Stage stage = getStageByLayerName(layer.getName());
        Viewport stageViewport = stage.getViewport();

        boolean isCustomViewport = (stageViewport != this.viewport);
        if (isCustomViewport) {
            if (batch.isDrawing()) {
                batch.flush();
            }

            tmpMatrix.set(batch.getProjectionMatrix());
            stageViewport.apply(true);
            batch.setProjectionMatrix(stageViewport.getCamera().combined);
        }

        stage.act(Gdx.graphics.getDeltaTime());
        stage.getRoot().draw(batch, 1f);

        if (isCustomViewport) {
            if (batch.isDrawing()) {
                batch.flush();
            }
            batch.setProjectionMatrix(tmpMatrix);
        }
    }

    public void updateOffsetsForGroupLayer(String[] layerName, float offsetX, float offsetY) {
        MapLayer mapLayer = getTiledLayer(layerName);
        mapLayer.setOffsetX(offsetX);
        mapLayer.setOffsetY(offsetY);

        if (!(mapLayer instanceof MapGroupLayer)) {
            throw new IllegalArgumentException("Layer [" + layerName + "] is not MapGroupLayer");
        }

        MapGroupLayer groupLayer = (MapGroupLayer) mapLayer;

        for (MapLayer layerLayer : groupLayer.getLayers()) {
            // Двигаем слой через его позицию, а не offset
            if (layerLayer instanceof TiledMapImageLayer) {
                TiledMapImageLayer imageLayer = (TiledMapImageLayer) layerLayer;
                imageLayer.setX(imageLayer.getX() + offsetX);
                imageLayer.setY(imageLayer.getY() + offsetY);
            } else {
                layerLayer.setOffsetX(layerLayer.getOffsetX() + offsetX);
                layerLayer.setOffsetY(layerLayer.getOffsetY() + offsetY);
            }
            layerLayer.invalidateRenderOffset();

            Stage stage = stages.get(layerLayer.getName());
            if (stage != null) {
                for (Actor actor : stage.getActors()) {
                    actor.setX(actor.getX() + offsetX);
                    actor.setY(actor.getY() + offsetY);
                }
            }
        }
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
        if (vfxManager != null) {
            vfxManager.resize(width, height);
        }

        for (Stage stage : stages.values()) {
            stage.getViewport().update(width, height, true);
        }
    }

    private void clampToEdgeTextures(TiledMap tiledMap) {
        for (MapLayer layer : tiledMap.getLayers()) {
            if (layer instanceof TiledMapImageLayer) {
                ((TiledMapImageLayer) layer).getTextureRegion().getTexture().setWrap(
                    Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge
                );
            }
        }
    }

    private void createStageActors(InputMultiplexer inputMultiplexer, MapLayers mapLayers) {
        for (MapLayer layer : mapLayers) {
            if (layer instanceof MapGroupLayer) {
                MapLayers layers = ((MapGroupLayer) layer).getLayers();
                createStageActors(inputMultiplexer, layers);
            }

            MapObjects objects = layer.getObjects();
            MapProperties layerProperties = layer.getProperties();
            Boolean isUiLayer = layerProperties.get(IS_UI_LAYER, Boolean.class);
            Boolean isObjectLayer = layerProperties.get(IS_OBJECT_LAYER, Boolean.class);
            if (Boolean.TRUE != isUiLayer &&
                Boolean.TRUE != isObjectLayer &&
                (objects == null || objects.getCount() == 0)
            ) {
                continue;
            }

            Stage stage = createStage(isUiLayer);
            for (MapObject object : objects) {
                Actor actor = actorFactory.createActor(layer.getName(), object);
                if (actor == null) {
                    Gdx.app.log("WARN", "layer=[" + layer.getName() +
                        "] objectName=[" + object.getName() + "] has not renderComponent"
                    );
                    continue;
                }

                actor.setName(object.getName());
                stage.addActor(actor);
            }

            stages.put(layer.getName(), stage);
            inputMultiplexer.addProcessor(stage);
        }
    }

    private Stage createStage(Boolean isUiLayer) {
        Stage stage;
        if (Boolean.TRUE.equals(isUiLayer)) {
            // Создаём отдельный статичный Viewport с собственной камерой для HUD
            Viewport layerViewport = new ExtendViewport(
                viewport.getWorldWidth(),
                viewport.getWorldHeight(),
                new OrthographicCamera()
            );
            stage = new Stage(layerViewport, getBatch());
        } else {
            stage = new Stage(viewport, getBatch());
        }
        return stage;
    }

    public InputProcessor getInputProcessor() {
        return inputMultiplexer;
    }

    public MapLayer getTiledLayer(String... layerName) {
        if (layerName.length == 1) {
            return getMap().getLayers().get(layerName[0]);
        } else if (layerName.length > 1) {
            MapLayers layers = getMap().getLayers();
            for (int i = 0; i < layerName.length; i++) {
                MapLayer mapLayer = layers.get(layerName[i]);
                if (i == layerName.length - 1) {
                    // если последний элемент, возвращаем слой
                    return mapLayer;
                }
                // иначе идем по группе слоев
                if (!(mapLayer instanceof MapGroupLayer)) {
                    throw new IllegalArgumentException("layer [" + layerName[i] + "] must be MapGroupLayer");
                }
                layers = ((MapGroupLayer) mapLayer).getLayers();
            }
        }
        throw new IllegalArgumentException("parameter 'layerName' must be >= 1");
    }

    public <T extends Actor> T getActor(String layerName, String actorName, Class<T> clazz) {
        Stage stage = stages.get(layerName);
        if (stage == null) {
            StringBuilder builder = new StringBuilder();
            int i = 0;
            for (String key : stages.keys()) {
                if (i > 0) {
                    builder.append(",");
                }
                builder.append(key);
                i++;
            }

            throw new IllegalArgumentException("unknown layer [" + layerName + "]. available object layers: " + builder.toString());
        }

        return clazz.cast(stage.getRoot().findActor(actorName));
    }

    public Stage getStageByLayerName(String layerName) {
        Stage stage = stages.get(layerName);
        if (stage == null) {
            throw new IllegalArgumentException("stage [" + layerName + "] is not found");
        }
        return stage;
    }

    public void addToStage(String layerName, Actor actor) {
        Stage stage = getStageByLayerName(layerName);
        stage.addActor(actor);
    }

    public boolean isVfxEnabled() {
        return isVfxEnabled;
    }

}
