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

public class UserActorFactory implements IUserActorFactory {


    public static final String IS_ECS_ENTITY = "is_ecs_entity";
    private final DefaultActorFactory delegate;

    private final Skin skin;
    private final AssetManager assetManager;

    private EntityFactory entityFactory;

    private final String atlasName;

    public UserActorFactory(Skin skin, AssetManager assetManager, String atlasName) {
        this.delegate = new DefaultActorFactory(skin, assetManager);
        this.skin = skin;
        this.assetManager = assetManager;
        this.atlasName = atlasName;
    }

    @Override
    public Actor getActor(MapObject object) {
        MapProperties properties = object.getProperties();

        String actorType = properties.get(DefaultActorFactory.ACTOR_TYPE, String.class);
        ActorTypeEnum actorTypeEnum = getActorTypeEnum(actorType);

        Boolean isEcsEntity = properties.get(IS_ECS_ENTITY, Boolean.class);

        if (isEcsEntity != null && isEcsEntity) {
            try {
                return createEcsEntity(actorTypeEnum, properties);
            } catch (Exception e) {
                throw new GdxRuntimeException("UserActorFactory#createEcsEntity: object=[" + object.getName() + "] " + e.getMessage());
            }
        }

        Actor actor = getCustomUiActor(actorTypeEnum);
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

    public Actor getCustomUiActor(ActorTypeEnum actorType) {
        switch (actorType) {
            case BED:
                return delegate.createImage(atlasName, Resources.TextureRegion.BED);
            case BED2:
                return delegate.createImage(atlasName, Resources.TextureRegion.BED2);
            case BLIN:
                return delegate.createImage(atlasName, Resources.TextureRegion.BLIN);
            case CAB:
                return delegate.createImage(atlasName, Resources.TextureRegion.CAB);
            case CAM_1:
                return delegate.createImage(atlasName, Resources.TextureRegion.CAM_1);
            case CAM_2:
                return delegate.createImage(atlasName, Resources.TextureRegion.CAM_2);
            case CAM_3:
                return delegate.createImage(atlasName, Resources.TextureRegion.CAM_3);
            case CHA:
                return delegate.createImage(atlasName, Resources.TextureRegion.CHA);
            case CHA_2:
                return delegate.createImage(atlasName, Resources.TextureRegion.CHA_2);
            case CHAR:
                return delegate.createImage(atlasName, Resources.TextureRegion.CHAR);
            case CHAR_1:
                return delegate.createImage(atlasName, Resources.TextureRegion.CHAR_1);
            case CHAR_2:
                return delegate.createImage(atlasName, Resources.TextureRegion.CHAR_2);
            case DOOR_1:
                return delegate.createImage(atlasName, Resources.TextureRegion.DOOR_1);
            case DOOR_2:
                return delegate.createImage(atlasName, Resources.TextureRegion.DOOR_2);
            case DOOR_3:
                return delegate.createImage(atlasName, Resources.TextureRegion.DOOR_3);
            case DOOR_4:
                return delegate.createImage(atlasName, Resources.TextureRegion.DOOR_4);
            case KEY:
                return delegate.createImage(atlasName, Resources.TextureRegion.KEY);
            case KEY_2:
                return delegate.createImage(atlasName, Resources.TextureRegion.KEY_2);
            case KEY_ITM:
                return delegate.createImage(atlasName, Resources.TextureRegion.KEY_ITM);
            case MED_1:
                return delegate.createImage(atlasName, Resources.TextureRegion.MED_1);
            case MED_2:
                return delegate.createImage(atlasName, Resources.TextureRegion.MED_2);
            case PUMP:
                return delegate.createImage(atlasName, Resources.TextureRegion.PUMP);
            case SAN:
                return delegate.createImage(atlasName, Resources.TextureRegion.SAN);
            case SAN_2:
                return delegate.createImage(atlasName, Resources.TextureRegion.SAN_2);
            case TABL:
                return delegate.createImage(atlasName, Resources.TextureRegion.TABL);
            case TABLE_COMP:
                return delegate.createImage(atlasName, Resources.TextureRegion.TABLE_COMP);
            case TRA_1:
                return delegate.createImage(atlasName, Resources.TextureRegion.TRA_1);
            case TRA_2:
                return delegate.createImage(atlasName, Resources.TextureRegion.TRA_2);
            case WAT:
                return delegate.createImage(atlasName, Resources.TextureRegion.WAT);
            case WAT_2:
                return delegate.createImage(atlasName, Resources.TextureRegion.WAT_2);


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
