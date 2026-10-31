package com.github.br.syberian.jam26;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.br.libgdx.common.structure.screen.ui.ActorFactory;
import com.github.br.libgdx.common.structure.screen.ui.DefaultActorFactory;
import com.github.br.syberian.jam26.ecs.components.RenderComponent;

import org.jspecify.annotations.NonNull;

public class UserActorFactory implements ActorFactory {


    public static final String IS_ECS_ENTITY = "is_ecs_entity";
    private final DefaultActorFactory delegate;

    private final Skin skin;
    private final AssetManager assetManager;

    private EntityFactory entityFactory;

    public UserActorFactory(Skin skin, AssetManager assetManager) {
        this.delegate = new DefaultActorFactory(skin, assetManager);
        this.skin = skin;
        this.assetManager = assetManager;
    }

    @Override
    public Actor getActor(MapObject object) {
        MapProperties properties = object.getProperties();

        String actorType = properties.get(DefaultActorFactory.ACTOR_TYPE, String.class);
        ActorTypeEnum actorTypeEnum = getActorTypeEnum(actorType);

        Boolean isEcsEntity = properties.get(IS_ECS_ENTITY, Boolean.class);

        if (isEcsEntity != null && isEcsEntity) {
            return createEcsEntity(actorTypeEnum, properties);
        }

        Actor actor = getCustomActor(actorTypeEnum);
        if (actor != null) {
            return actor;
        }

        return delegate.getActor(object);
    }

    private @NonNull Actor createEcsEntity(ActorTypeEnum actorTypeEnum, MapProperties properties) {
        int id = entityFactory.createEntityFromUI(actorTypeEnum, properties);
        World world = entityFactory.getWorld();
        ComponentMapper<RenderComponent> mapper = world.getMapper(RenderComponent.class);
        if (mapper == null) {
            throw new IllegalStateException("forgot RenderComponent for actorType=" + actorTypeEnum);
        }
        RenderComponent renderComponent = mapper.get(id);
        if (renderComponent == null) {
            Gdx.app.log("WARN", "UserActorFactory#createEcsEntity, actorTypeEnum=" +
                actorTypeEnum + " render component is null");
            return null;
        }

        if (renderComponent.actor == null) {
            throw new IllegalStateException("renderComponent.actor is null for actorType=" + actorTypeEnum);
        }

        return renderComponent.actor;
    }

    private static @NonNull ActorTypeEnum getActorTypeEnum(String actorType) {
        ActorTypeEnum actorTypeEnum;
        try {
            actorTypeEnum = ActorTypeEnum.valueOf(actorType);
        } catch (Exception exception) {
            throw new GdxRuntimeException("actorType=" + actorType + " is not found");
        }
        return actorTypeEnum;
    }

    public Actor getCustomActor(ActorTypeEnum actorType) {

        switch (actorType) {
            case HERO:
                return delegate.createImage(Resources.Atlases.GAME_OBJECTS, Resources.TextureRegion.HERO);
            case ENEMY:
                return delegate.createImage(Resources.Atlases.GAME_OBJECTS, Resources.TextureRegion.ENEMY);
            case CAMERA:
                return delegate.createImage(Resources.Atlases.GAME_OBJECTS, Resources.TextureRegion.CAMERA);
            case BOX:
                return delegate.createImage(Resources.Atlases.GAME_OBJECTS, Resources.TextureRegion.BOX);
            case DOOR:
                return delegate.createImage(Resources.Atlases.GAME_OBJECTS, Resources.TextureRegion.DOOR);
            default:
                throw new GdxRuntimeException(
                    "com.github.br.syberian.jam26.UserActorFactory.getCustomActor: " +
                        actorType + " is not found"
                );
        }
    }

    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

}
