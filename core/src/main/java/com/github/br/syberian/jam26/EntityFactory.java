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
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.br.syberian.jam26.ecs.components.AnimationComponent;
import com.github.br.syberian.jam26.ecs.components.EnemyComponent;
import com.github.br.syberian.jam26.ecs.components.PhysicsComponent;
import com.github.br.syberian.jam26.ecs.components.PlayerComponent;
import com.github.br.syberian.jam26.ecs.components.RenderComponent;
import com.github.br.syberian.jam26.ecs.components.TransformComponent;
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

    protected ComponentMapper<PlayerComponent> heroMapper;
    protected ComponentMapper<EnemyComponent> enemyMapper;

    private UserActorFactory userActorFactory;

    public EntityFactory(AssetManager assetManager) {
        this.assetManager = assetManager;
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
        heroMapper = getWorld().getMapper(PlayerComponent.class);
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

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.DynamicBody, x, y, width, height
        );

        //TODO пока анимации нету
//        AnimationComponent animationComponent = animationMapper.get(id);
//        animationComponent.simpleAnimationComponent = AnimationFactory.createHero();

        RenderComponent renderComponent = renderMapper.get(id);
        renderComponent.actor =  userActorFactory.getCustomActor(ActorTypeEnum.HERO);

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
        renderComponent.actor =  userActorFactory.getCustomActor(ActorTypeEnum.HERO);

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
        BodyDef bodyDef = new BodyDef();
        // We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = bodyType;
        // Set our body's starting position in the world
        bodyDef.position.set(x, y);

        // Create our body in the world using our body definition
        PhysicsSystem physics = getWorld().getSystem(PhysicsSystem.class);
        World box2dWorld = physics.getBox2dWorld();
        Body body = box2dWorld.createBody(bodyDef);

        // Create a circle shape and set its radius to 6
        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(width, height);

        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = groundBox;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f; // Make it bounce a little bit

        // Create our fixture and attach it to the body
        Fixture fixture = body.createFixture(fixtureDef);

        // Remember to dispose of any shapes after you're done with them!
        // BodyDef and FixtureDef don't need disposing, but shapes do.
        groundBox.dispose();

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
