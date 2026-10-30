package com.github.br.libgdx.common.structure.screen.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.github.br.libgdx.common.structure.screen.ui.vfx.VfxEffectPooledFactory;

public class CustomOrthogonalTiledMapRenderer extends OrthogonalTiledMapRenderer {

    public static final String IS_OBJECT_LAYER = "is_object_layer";
    public static final String IS_VFX_EFFECT_ENABLED = "is_vfx_effect_enabled";
    public static final String VFX_LAYER_EFFECT = "vfx_layer_effect";

    private final ObjectMap<String, Stage> stages = new ObjectMap<>();
    private final Viewport viewport;
    private final ActorFactory actorFactory;
    private final InputMultiplexer inputMultiplexer;

    private final VfxEffectPooledFactory vfxEffectPooledFactory;
    private VfxManager vfxManager;
    private boolean isVfxEnabled;

    public CustomOrthogonalTiledMapRenderer(
        ActorFactory actorFactory,
        Viewport viewport,
        TiledMap map,
        float unitScale
    ) {
        this(actorFactory, viewport, map, unitScale, false, null, 0, 0);
    }

    public CustomOrthogonalTiledMapRenderer(
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
        // Рендерим Stage
        Stage stage = getStageByLayerName(layer.getName());
        stage.getViewport().apply();
        stage.act(Gdx.graphics.getDeltaTime());
        stage.getRoot().draw(batch, 1f);
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
            Boolean isObjectLayer = layerProperties.get(IS_OBJECT_LAYER, Boolean.class);
            if (Boolean.TRUE != isObjectLayer && (objects == null || objects.getCount() == 0)) {
                continue;
            }

            Stage stage = new Stage(viewport, getBatch());
            for (MapObject object : objects) {
                Actor actor = actorFactory.getActor(object);
                MapProperties properties = object.getProperties();
                float x = properties.get("x", float.class);
                float y = properties.get("y", float.class);
                float width = properties.get("width", Float.class);
                float height = properties.get("height", Float.class);
                actor.setBounds(x, y, width, height);

                actor.setName(object.getName());
                stage.addActor(actor);
            }

            stages.put(layer.getName(), stage);
            inputMultiplexer.addProcessor(stage);
        }
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

    public boolean isVfxEnabled() {
        return isVfxEnabled;
    }

}
