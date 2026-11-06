package com.github.br.syberian.jam26;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Null;
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
    public Actor getActor(MapObject mapObject) {
        MapProperties properties = mapObject.getProperties();

        float x = properties.get("x", float.class);
        float y = properties.get("y", float.class);
        float width = properties.get("width", float.class);
        float height = properties.get("height", float.class);

        float originX = 0, originY = 0, rotationDegrees = 0;
        if (mapObject instanceof TextureMapObject) {
            TextureMapObject textureMapObject = (TextureMapObject) mapObject;
            originX = textureMapObject.getOriginX();
            originY = textureMapObject.getOriginY();
            rotationDegrees = -textureMapObject.getRotation();
        }

        String actorType = properties.get(DefaultActorFactory.ACTOR_TYPE, String.class);
        ActorTypeEnum actorTypeEnum = getActorTypeEnum(actorType);

        Actor actor = null;
        Boolean isEcsEntity = properties.get(IS_ECS_ENTITY, Boolean.class);
        if (isEcsEntity != null && isEcsEntity) {
            try {
                actor = createEcsEntity(actorTypeEnum, mapObject, x, y, width, height, originX, originY, rotationDegrees);
            } catch (Exception e) {
                throw new GdxRuntimeException("UserActorFactory#createEcsEntity: object=[" + mapObject.getName() + "] " + e.getMessage());
            }
        } else {
            actor = getCustomUiActor(actorTypeEnum);
            if (actor == null) {
                actor = delegate.getActor(mapObject);
            }
        }

        if (actor != null) {
            actor.setX(x);
            actor.setY(y);
            actor.setWidth(width);
            actor.setHeight(height);
            actor.setOriginX(originX);
            actor.setOriginY(originY);
            actor.setRotation(rotationDegrees);
        }

        return actor;
    }

    private @Null Actor createEcsEntity(
        ActorTypeEnum actorTypeEnum,
        MapObject mapObject, float x,
        float y,
        float width,
        float height,
        float originX,
        float originY,
        float rotation
    ) {
        int id = entityFactory.createEntityFromUI(
            actorTypeEnum, mapObject, x, y, width, height, originX, originY, rotation
        );
        World world = entityFactory.getWorld();
        ComponentMapper<RenderComponent> mapper = world.getMapper(RenderComponent.class);
        if (mapper == null) {
            throw new IllegalStateException("forgot RenderComponent for actorType=" + actorTypeEnum);
        }
        RenderComponent renderComponent = mapper.get(id);
        if (renderComponent == null || renderComponent.actor == null) {
            Gdx.app.log("WARN", "UserActorFactory#createEcsEntity, actorTypeEnum=" +
                actorTypeEnum + " render component is null");
            return null;
        }

        return renderComponent.actor;
    }

    private static @NonNull ActorTypeEnum getActorTypeEnum(String actorType) {
        ActorTypeEnum actorTypeEnum;
        try {
            actorTypeEnum = ActorTypeEnum.valueOf(actorType);
            return actorTypeEnum;
        } catch (Exception exception) {
            throw new GdxRuntimeException("actorType=" + actorType + " is not found");
        }
    }

    public Actor getCustomUiActor(ActorTypeEnum actorType) {
        switch (actorType) {
            case BOX_OPEN:
            case KOROB_OPEN:
                return delegate.createImage(atlasName, Resources.TextureRegion.BOX_OPEN);
            case BOX:
            case KOROB:
                return delegate.createImage(atlasName, Resources.TextureRegion.BOX_CLOSED);
            case BED:
                return delegate.createImage(atlasName, Resources.TextureRegion.BED_GREEN);
            case BED2:
                return delegate.createImage(atlasName, Resources.TextureRegion.BED_GRAY);
            case BLIN:
                return delegate.createImage(atlasName, Resources.TextureRegion.BLIN);
            case CAB:
                return delegate.createImage(atlasName, Resources.TextureRegion.CAB);
            case CAMERA:
            case CAM_1:
                return delegate.createAnimatedImage(
                    atlasName, Resources.TextureRegion.CAM_1, Animation.PlayMode.NORMAL, 0f
                );
            case CAM_2:
            case CAMERA_2:
                return delegate.createImage(atlasName, Resources.TextureRegion.CAM_2);
            case CHA:
                return delegate.createImage(atlasName, Resources.TextureRegion.CHA);
            case CHA_2:
                return delegate.createImage(atlasName, Resources.TextureRegion.CHA_2);
            case CHAR: // hero
            case NPC:
                return delegate.createAnimatedImage(
                    atlasName, Resources.TextureRegion.HERO, Animation.PlayMode.NORMAL, 0f
                );
            case DOOR_GRAY:
                return delegate.createAnimatedImage(atlasName, Resources.TextureRegion.DOOR_GRAY, Animation.PlayMode.NORMAL, 0f);
            case DOOR_BROWN:
                return delegate.createAnimatedImage(atlasName, Resources.TextureRegion.DOOR_BROWN, Animation.PlayMode.NORMAL, 0f);
            case KEY:
                return delegate.createAnimatedImage(atlasName, Resources.TextureRegion.KEY, Animation.PlayMode.NORMAL, 0f);
            case KEY_ITM:
                return delegate.createImage(atlasName, Resources.TextureRegion.KEY_ITM);
            case MED:
                return delegate.createAnimatedImage(atlasName, Resources.TextureRegion.MED, Animation.PlayMode.NORMAL, 0f);
            case LEVER: // РЫЧАГ
                return delegate.createAnimatedImage(atlasName, Resources.TextureRegion.LEVER, Animation.PlayMode.NORMAL, 0f);
            case SECURITY:
                return delegate.createAnimatedImage(atlasName, Resources.TextureRegion.SECURITY, Animation.PlayMode.NORMAL, 0f);
            case IRON_CABINET: // железный шкаф
                return delegate.createAnimatedImage(atlasName, Resources.TextureRegion.IRON_CABINET, Animation.PlayMode.NORMAL, 0f);
            case CART: // тележка
                return delegate.createAnimatedImage(atlasName, Resources.TextureRegion.CART, Animation.PlayMode.NORMAL, 0f);
            case PUMP:
                return delegate.createImage(atlasName, Resources.TextureRegion.PUMP);
            case TOILET:
            case SAN:
                return delegate.createTestImage(atlasName, Resources.TextureRegion.SAN);
            case SINK:
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
            case COOLER:
                return delegate.createImage(atlasName, Resources.TextureRegion.COOLER);
            case CUP:
                return delegate.createImage(atlasName, Resources.TextureRegion.CUP);
            case WIRES:
                return delegate.createImage(atlasName, Resources.TextureRegion.WIRES);
            case WIRE_CUTTERS:
                return delegate.createImage(atlasName, Resources.TextureRegion.WIRE_CUTTERS);
            case TABLE:
                return delegate.createImage(atlasName, Resources.TextureRegion.TABLE);
            case CHAIR:
                return delegate.createImage(atlasName, Resources.TextureRegion.CHAIR);
            case BED_WHITE:
                return delegate.createImage(atlasName, Resources.TextureRegion.BED_GRAY);
            case BED_GREEN:
                return delegate.createImage(atlasName, Resources.TextureRegion.BED_GREEN);
            case SPORT_BENCH:
                return delegate.createImage(atlasName, Resources.TextureRegion.SPORT_BENCH);
            case FID_SPORT_BENCH:
                return delegate.createImage(atlasName, Resources.TextureRegion.FID_SPORT_BENCH);
            case DINING_BENCH:
                return delegate.createImage(atlasName, Resources.TextureRegion.DINING_BENCH);
            case SECURITY_MONITORS:
                return delegate.createImage(atlasName, Resources.TextureRegion.SECURITY_MONITORS);
            case GATE:
                return delegate.createImage(atlasName, Resources.TextureRegion.GATE);
            case PUMPKIN:
                return delegate.createImage(atlasName, Resources.TextureRegion.PUMPKIN);
            case BARBELL:
                return delegate.createImage(atlasName, Resources.TextureRegion.BARBELL);
            case WEIGHT_PLATE:
                return delegate.createImage(atlasName, Resources.TextureRegion.WEIGHT_PLATE);

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
