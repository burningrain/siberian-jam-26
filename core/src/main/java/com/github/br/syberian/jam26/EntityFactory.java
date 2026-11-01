package com.github.br.syberian.jam26;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
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
import com.github.br.syberian.jam26.ecs.components.SecurityCameraComponent;
import com.github.br.syberian.jam26.ecs.components.TransformComponent;
import com.github.br.syberian.jam26.ecs.components.TriggerComponent;
import com.github.br.syberian.jam26.ecs.systems.physics.contact.ContactDataImpl;
import com.github.br.syberian.jam26.ecs.systems.physics.PhysicsSystem;
import com.github.br.syberian.jam26.ecs.systems.physics.TriggerContactDataImpl;
import com.github.br.syberian.jam26.ecs.systems.physics.contact.PlayerContactDataImpl;

import box2dLight.ConeLight;

public class EntityFactory extends BaseSystem {

    private Archetype playerArchetype;

    private Archetype triggerArchetype;
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

    protected ComponentMapper<TriggerComponent> mTrigger;

    protected ComponentMapper<SecurityCameraComponent> cameraLogicMapper;

    private IUserActorFactory userActorFactory;

    private final GameManager gameManager;

    public EntityFactory(GameManager gameManager) {
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
        mTrigger = getWorld().getMapper(TriggerComponent.class);
        cameraLogicMapper = getWorld().getMapper(SecurityCameraComponent.class);

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

        triggerArchetype = new ArchetypeBuilder()
            .add(TransformComponent.class)
            .add(PhysicsComponent.class)
            .add(TriggerComponent.class)
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
            .add(SecurityCameraComponent.class)
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

    /**
     * Для самих триггеров на карте (зон) делайте обычные StaticBody,
     * но у их Fixture тоже выставляйте fixtureDef.isSensor = true;,
     * чтобы игрок мог проходить сквозь них
     */
    public int createTrigger(
        TriggerType triggerType,
        TriggerComponent.UseType useType,
        TriggerComponent.ConditionType conditionType,
        float x,
        float y,
        float width,
        float height
    ) {
        int id = world.create(triggerArchetype);

        TransformComponent transformComponent = transformMapper.get(id);
        transformComponent.x = x;
        transformComponent.y = y;

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.StaticBody, true, x, y, width, height
        );


        TriggerComponent trigger = mTrigger.get(id);
        trigger.conditionType = conditionType;
        trigger.useType = useType;

        // Передаем кастомный код текстом/логикой:
        TriggerActionFactory triggerActionFactory = getWorld().getSystem(TriggerActionFactory.class);
        trigger.action = triggerActionFactory.create(triggerType);

        return id;
    }

    public int createPlayer(ActorTypeEnum actorType, float x, float y, float width, float height) {
        SimpleUtils simpleUtils = gameManager.simpleUtils;
        int id = world.create(playerArchetype);

        TransformComponent transformComponent = transformMapper.get(id);
        transformComponent.x = x;
        transformComponent.y = y;
        transformComponent.rotation = 0f;

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.DynamicBody, false, x, y, width, height
        );

        // сенсор для столкновений и триггеров
        Body body = physicsComponent.body;
        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(
            simpleUtils.convertUnitsToMetres(width / 2 * 1.3f),
            simpleUtils.convertUnitsToMetres(height / 2 * 1.3f)
        );

        FixtureDef sensorDef = new FixtureDef();
        sensorDef.shape = sensorShape;
        sensorDef.isSensor = true;

        Fixture sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(new PlayerContactDataImpl(id));
        sensorShape.dispose();

        PlayerComponent playerComponent = playerMapper.get(id);

        //TODO пока анимации нету
//        AnimationComponent animationComponent = animationMapper.get(id);
//        animationComponent.simpleAnimationComponent = AnimationFactory.createHero();

        RenderComponent renderComponent = renderMapper.get(id);
        renderComponent.actor = userActorFactory.getCustomUiActor(actorType);

        return id;
    }

    private int createEnemy(ActorTypeEnum actorType, float x, float y, float width, float height) {
        int id = world.create(enemyArchetype);

        TransformComponent transformComponent = transformMapper.get(id);
        transformComponent.x = x;
        transformComponent.y = y;

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.DynamicBody, false, x, y, width, height
        );

        //TODO пока анимации нету
//        AnimationComponent animationComponent = animationMapper.get(id);
//        animationComponent.simpleAnimationComponent = AnimationFactory.createHero();

        RenderComponent renderComponent = renderMapper.get(id);
        renderComponent.actor =  userActorFactory.getCustomUiActor(actorType);

        return id;
    }

    public int createWall(ActorTypeEnum actorType, float x, float y, float width, float height) {
        int id = world.create(wallArchetype);

        TransformComponent transformComponent = transformMapper.get(id);
        transformComponent.x = x;
        transformComponent.y = y;

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.StaticBody, false, x, y, width, height
        );

        return id;
    }

    public int createBullet(ActorTypeEnum actorType, float x, float y, float width, float height, Vector2 direction) {
        int id = world.create(bulletArchetype);

        TransformComponent transformComponent = transformMapper.get(id);
        transformComponent.x = x;
        transformComponent.y = y;

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.DynamicBody, false, x, y, width, height
        );

        return id;
    }

    public int createDoor(ActorTypeEnum actorType, float x, float y, float width, float height) {
        int id = world.create(doorArchetype);

        TransformComponent transformComponent = transformMapper.get(id);
        transformComponent.x = x;
        transformComponent.y = y;

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.DynamicBody, false, x, y, width, height
        );

        RenderComponent renderComponent = renderMapper.get(id);
        renderComponent.actor =  userActorFactory.getCustomUiActor(actorType);

        return id;
    }

    private int createCamera(ActorTypeEnum actorType, float x, float y, float width, float height) {
        int id = world.create(cameraArchetype);

        TransformComponent transformComponent = transformMapper.get(id);
        transformComponent.x = x;
        transformComponent.y = y;

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.KinematicBody, false, x, y, width, height
        );

        SecurityCameraComponent cam = cameraLogicMapper.get(id);

        // --- АВТОМАТИЧЕСКИЙ РАСЧЕТ СМЕЩЕНИЯ ИЗ ФОРМЫ ТЕЛА ---
        if (physicsComponent.body != null && !physicsComponent.body.getFixtureList().isEmpty()) {
            com.badlogic.gdx.physics.box2d.Fixture fixture = physicsComponent.body.getFixtureList().first();
            if (fixture.getShape() instanceof com.badlogic.gdx.physics.box2d.PolygonShape) {
                com.badlogic.gdx.physics.box2d.PolygonShape shape = (com.badlogic.gdx.physics.box2d.PolygonShape) fixture.getShape();
                Vector2 tmpVertex = new Vector2();

                // Забираем правый верхний угол (индекс 2) в ЛОКАЛЬНЫХ МЕТРАХ Box2D формы.
                // tmpVertex.x будет в точности равен половине ширины тела камеры в метрах!
                shape.getVertex(2, tmpVertex);

                // Выставляем точку испускания луча строго на правый край камеры
                cam.localLightOffset.set(tmpVertex.x, 0f);
            }
        }

        // Инициализируем свет с учетом динамического оффсета
        SimpleUtils simpleUtils = gameManager.simpleUtils;
        PhysicsSystem physics = world.getSystem(PhysicsSystem.class);

        // Считаем стартовую мировую позицию объектива (с учетом смещения)
        Vector2 startingWorldPos = physicsComponent.body.getWorldPoint(cam.localLightOffset);

        // Находим стартовую мировую позицию объектива
        Vector2 bodyCenterPos = physicsComponent.body.getPosition();

        // Считаем смещение с учетом стартового угла
        cam.worldLightPosMetres.set(cam.localLightOffset);
        cam.worldLightPosMetres.rotateRad(physicsComponent.body.getAngle());
        cam.worldLightPosMetres.add(bodyCenterPos);

        float pixelX = simpleUtils.convertMetresToUnits(cam.worldLightPosMetres.x);
        float pixelY = simpleUtils.convertMetresToUnits(cam.worldLightPosMetres.y);
        float pixelRange = simpleUtils.convertMetresToUnits(cam.cameraRange);

        cam.cameraLight = new ConeLight(
            physics.getRayHandler(),
            120,
            com.badlogic.gdx.graphics.Color.YELLOW,
            pixelRange,
            pixelX,
            pixelY,
            cam.viewDirectionDegrees,
            cam.fovDegrees
        );

        RenderComponent renderComponent = renderMapper.get(id);
        renderComponent.actor = userActorFactory.getCustomUiActor(actorType);

        return id;
    }

    private int createBox(ActorTypeEnum actorType, float x, float y, float width, float height) {
        int id = world.create(boxArchetype);

        TransformComponent transformComponent = transformMapper.get(id);
        transformComponent.x = x;
        transformComponent.y = y;

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.DynamicBody, false, x, y, width, height
        );

        RenderComponent renderComponent = renderMapper.get(id);
        renderComponent.actor =  userActorFactory.getCustomUiActor(actorType);

        return id;
    }

    private PhysicsComponent createPhysicsComponent(
        int entityId,
        BodyDef.BodyType bodyType,
        boolean isSensor,
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
        fixtureDef.isSensor = isSensor;
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
        body.setUserData(isSensor? new TriggerContactDataImpl(entityId) : new ContactDataImpl(entityId));

        PhysicsComponent physicsComponent = physicsMapper.get(entityId);
        physicsComponent.body = body;

        return physicsComponent;
    }

    public void setUserActorFactory(IUserActorFactory userActorFactory) {
        this.userActorFactory = userActorFactory;
    }

    public int createEntityFromUI(ActorTypeEnum actorType, MapProperties properties) {
        float x = properties.get("x", float.class);
        float y = properties.get("y", float.class);
        float width = properties.get("width", float.class);
        float height = properties.get("height", float.class);

        switch (actorType) {
            case HERO:
            case CHAR:
            case CHAR_1:
            case CHAR_2:
                return createPlayer(actorType, x, y, width, height);
            case TRIGGER:
                String triggerType = properties.get("trigger_type", String.class);
                String useType = properties.get("use_type", String.class);
                String conditionType = properties.get("condition_type", String.class);

                TriggerType triggerTypeEnum;
                TriggerComponent.UseType useTypeEnum;
                TriggerComponent.ConditionType conditionTypeEnum;
                try {
                    triggerTypeEnum = TriggerType.valueOf(triggerType);
                    useTypeEnum = TriggerComponent.UseType.valueOf(useType);
                    conditionTypeEnum = TriggerComponent.ConditionType.valueOf(conditionType);
                } catch (Exception e) {
                    throw new GdxRuntimeException("TRIGGER params are not valid");
                }

                return createTrigger(triggerTypeEnum, useTypeEnum, conditionTypeEnum, x, y, width, height);
            case ENEMY:
                return createEnemy(actorType, x, y, width, height);
            case BOX:
            case KOROB_OPEN:
                return createBox(actorType, x, y, width, height);
            case CAMERA:
            case CAM_2:
            case CAM_3:
                return createCamera(actorType, x, y, width, height);
            case BULLET:
                return createBullet(actorType, x, y, width, height, new Vector2());
            case WALL:
                return createWall(actorType, x, y, width, height);
            case DOOR:
                return createDoor(actorType, x, y, width, height);
        }

        throw new GdxRuntimeException("createEntityFromUI: " + actorType + " is not found");
    }

}
