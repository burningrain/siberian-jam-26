package com.github.br.syberian.jam26;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.br.libgdx.common.structure.GameManager;
import com.github.br.libgdx.common.structure.SimpleUtils;
import com.github.br.syberian.jam26.ecs.components.AnimationComponent;
import com.github.br.syberian.jam26.ecs.components.EnemyComponent;
import com.github.br.syberian.jam26.ecs.components.PhysicsComponent;
import com.github.br.syberian.jam26.ecs.components.PlayerComponent;
import com.github.br.syberian.jam26.ecs.components.RenderComponent;
import com.github.br.syberian.jam26.ecs.components.TransformComponent;
import com.github.br.syberian.jam26.ecs.systems.physics.ContactDataImpl;
import com.github.br.syberian.jam26.ecs.systems.physics.PhysicsSystem;

public class EntityFactory extends BaseSystem {

    private final AssetManager assetManager;

    private Archetype playerArchetype;
    private Archetype enemyArchetype;
    private Archetype wallArchetype;
    private Archetype bulletArchetype;
    private Archetype boxArchetype;
    private Archetype cameraArchetype;
    private Archetype doorArchetype;

    protected ComponentMapper<TransformComponent> transformMapper;
    protected ComponentMapper<PhysicsComponent> physicsMapper;
    protected ComponentMapper<AnimationComponent> animationMapper;
    protected ComponentMapper<RenderComponent> renderMapper;

    protected ComponentMapper<PlayerComponent> playerMapper;
    protected ComponentMapper<EnemyComponent> enemyMapper;

    private UserActorFactory userActorFactory;

    private final GameManager gameManager;

    public EntityFactory(AssetManager assetManager, GameManager gameManager) {
        this.assetManager = assetManager;
        this.gameManager = gameManager;
    }

    @Override
    public com.artemis.World getWorld() {
        return world;
    }

    @Override
    protected void initialize() {
        transformMapper = getWorld().getMapper(TransformComponent.class);
        physicsMapper = getWorld().getMapper(PhysicsComponent.class);
        animationMapper = getWorld().getMapper(AnimationComponent.class);
        renderMapper = getWorld().getMapper(RenderComponent.class);
        playerMapper = getWorld().getMapper(PlayerComponent.class);
        enemyMapper = getWorld().getMapper(EnemyComponent.class);

        setEnabled(false);  // Отключаем ежекадровое обновление

        playerArchetype = new ArchetypeBuilder()
            .add(TransformComponent.class)
            .add(PhysicsComponent.class)
            .add(PlayerComponent.class)
            .add(AnimationComponent.class)
            .add(RenderComponent.class)
            .build(world);

        enemyArchetype = new ArchetypeBuilder()
            .add(TransformComponent.class)
            .add(PhysicsComponent.class)
            .add(EnemyComponent.class)
            .add(AnimationComponent.class)
            .add(RenderComponent.class)
            .build(world);

        wallArchetype = new ArchetypeBuilder()
            .add(TransformComponent.class)
            .add(PhysicsComponent.class)
            .build(world);

        bulletArchetype = new ArchetypeBuilder()
            .add(TransformComponent.class)
            .add(PhysicsComponent.class)
            .add(RenderComponent.class)
            .build(world);

        boxArchetype = new ArchetypeBuilder()
            .add(TransformComponent.class)
            .add(PhysicsComponent.class)
            .add(RenderComponent.class)
            .build(world);

        cameraArchetype = new ArchetypeBuilder()
            .add(TransformComponent.class)
            .add(PhysicsComponent.class)
            .add(RenderComponent.class)
            .build(world);

        doorArchetype = new ArchetypeBuilder()
            .add(TransformComponent.class)
            .add(PhysicsComponent.class)
            .add(RenderComponent.class)
            .build(world);
    }

    @Override
    protected void processSystem() {
    }

    public int createPlayer(float x, float y, float width, float height) {
        int id = world.create(playerArchetype);

        TransformComponent transformComponent = transformMapper.get(id);
        transformComponent.x = x;
        transformComponent.y = y;
        transformComponent.rotation = 0f;

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.DynamicBody, x, y, width, height
        );

        PlayerComponent playerComponent = playerMapper.get(id);

        //TODO пока анимации нету
//        AnimationComponent animationComponent = animationMapper.get(id);
//        animationComponent.simpleAnimationComponent = AnimationFactory.createHero();

        RenderComponent renderComponent = renderMapper.get(id);
        renderComponent.actor = userActorFactory.getCustomActor(ActorTypeEnum.HERO);

        return id;
    }

    private int createEnemy(float x, float y, float width, float height) {
        int id = world.create(enemyArchetype);

        TransformComponent transformComponent = transformMapper.get(id);
        transformComponent.x = x;
        transformComponent.y = y;

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.DynamicBody, x, y, width, height
        );

        //TODO пока анимации нету
//        AnimationComponent animationComponent = animationMapper.get(id);
//        animationComponent.simpleAnimationComponent = AnimationFactory.createHero();

        RenderComponent renderComponent = renderMapper.get(id);
        renderComponent.actor =  userActorFactory.getCustomActor(ActorTypeEnum.ENEMY);

        return id;
    }

    public int createWall(float x, float y, float width, float height) {
        int id = world.create(wallArchetype);

        TransformComponent transformComponent = transformMapper.get(id);
        transformComponent.x = x;
        transformComponent.y = y;

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.StaticBody, x, y, width, height
        );

        return id;
    }

    public int createBullet(float x, float y, float width, float height, Vector2 direction) {
        int id = world.create(bulletArchetype);

        TransformComponent transformComponent = transformMapper.get(id);
        transformComponent.x = x;
        transformComponent.y = y;

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.DynamicBody, x, y, width, height
        );

        return id;
    }

    public int createDoor(float x, float y, float width, float height) {
        int id = world.create(doorArchetype);

        TransformComponent transformComponent = transformMapper.get(id);
        transformComponent.x = x;
        transformComponent.y = y;

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.DynamicBody, x, y, width, height
        );

        RenderComponent renderComponent = renderMapper.get(id);
        renderComponent.actor =  userActorFactory.getCustomActor(ActorTypeEnum.CAMERA);

        return id;
    }

    private int createCamera(float x, float y, float width, float height) {
        int id = world.create(cameraArchetype);

        TransformComponent transformComponent = transformMapper.get(id);
        transformComponent.x = x;
        transformComponent.y = y;

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.DynamicBody, x, y, width, height
        );

        RenderComponent renderComponent = renderMapper.get(id);
        renderComponent.actor =  userActorFactory.getCustomActor(ActorTypeEnum.CAMERA);

        return id;
    }

    private int createBox(float x, float y, float width, float height) {
        int id = world.create(boxArchetype);

        TransformComponent transformComponent = transformMapper.get(id);
        transformComponent.x = x;
        transformComponent.y = y;

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.DynamicBody, x, y, width, height
        );

        RenderComponent renderComponent = renderMapper.get(id);
        renderComponent.actor =  userActorFactory.getCustomActor(ActorTypeEnum.BOX);

        return id;
    }

    private PhysicsComponent createPhysicsComponent(
        int entityId,
        BodyDef.BodyType bodyType,
        float x,
        float y,
        float width,
        float height
    ) {
        SimpleUtils simpleUtils = gameManager.simpleUtils;

        // 1. Вычисляем половины размеров, так как setAsBox принимает полупериметр
        float halfWidth = width / 2f;
        float halfHeight = height / 2f;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;

        // 2. Смещаем точку спавна из левого нижнего угла в геометрический центр объекта
        bodyDef.position.set(
            simpleUtils.convertUnitsToMetres(x + halfWidth),
            simpleUtils.convertUnitsToMetres(y + halfHeight)
        );

        PhysicsSystem physics = getWorld().getSystem(PhysicsSystem.class);
        World box2dWorld = physics.getBox2dWorld();
        Body body = box2dWorld.createBody(bodyDef);

        PolygonShape groundBox = new PolygonShape();
        // 3. Передаем именно половины ширины и высоты
        groundBox.setAsBox(
            simpleUtils.convertUnitsToMetres(halfWidth),
            simpleUtils.convertUnitsToMetres(halfHeight)
        );

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = groundBox;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        // Для Hotline Miami 0.6f — это многовато (коробки и враги будут сильно отскакивать при коллизиях),
        // лучше поставить поменьше, например 0.1f. Но можете оставить как вам нравится.
        fixtureDef.restitution = 0.1f;

        body.createFixture(fixtureDef);
        groundBox.dispose();

        // 4. Привязываем id сущности к телу Box2D.
        // Это спасет вас в будущем, когда вы будете писать обработку попадания пуль во врагов в ContactListener
        body.setUserData(new ContactDataImpl(entityId));

        PhysicsComponent physicsComponent = physicsMapper.get(entityId);
        physicsComponent.body = body;

        return physicsComponent;
    }

    public void setUserActorFactory(UserActorFactory userActorFactory) {
        this.userActorFactory = userActorFactory;
    }

    public int createEntityFromUI(ActorTypeEnum actorType, MapProperties properties) {
        float x = properties.get("x", float.class);
        float y = properties.get("y", float.class);
        float width = properties.get("width", float.class);
        float height = properties.get("height", float.class);

        switch (actorType) {
            case HERO:
                return createPlayer(x, y, width, height);
            case ENEMY:
                return createEnemy(x, y, width, height);
            case BOX:
                return createBox(x, y, width, height);
            case CAMERA:
                return createCamera(x, y, width, height);
            case BULLET:
                return createBullet(x, y, width, height, new Vector2());
            case WALL:
                return createWall(x, y, width, height);
            case DOOR:
                return createDoor(x, y, width, height);
        }

        throw new GdxRuntimeException("createEntityFromUI: " + actorType + " is not found");
    }

}
