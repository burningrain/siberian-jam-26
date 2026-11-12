package com.github.br.syberian.jam26;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Null;
import com.github.br.libgdx.common.structure.GameManager;
import com.github.br.libgdx.common.structure.SimpleUtils;
import com.github.br.syberian.jam26.ecs.components.EnemyComponent;
import com.github.br.syberian.jam26.ecs.components.FollowTheTargetComponent;
import com.github.br.syberian.jam26.ecs.components.HudComponent;
import com.github.br.syberian.jam26.ecs.components.InteractableStateComponent;
import com.github.br.syberian.jam26.ecs.components.ItemComponent;
import com.github.br.syberian.jam26.ecs.components.PhysicsComponent;
import com.github.br.syberian.jam26.ecs.components.PlayerComponent;
import com.github.br.syberian.jam26.ecs.components.RenderComponent;
import com.github.br.syberian.jam26.ecs.components.SecurityCameraComponent;
import com.github.br.syberian.jam26.ecs.components.TransformComponent;
import com.github.br.syberian.jam26.ecs.components.TriggerComponent;
import com.github.br.syberian.jam26.ecs.systems.UtilsObjectsHolder;
import com.github.br.syberian.jam26.ecs.systems.physics.CollisionType;
import com.github.br.syberian.jam26.ecs.systems.physics.PhysicsSystem;
import com.github.br.syberian.jam26.ecs.systems.physics.contact.ContactData;
import com.github.br.syberian.jam26.ecs.systems.physics.contact.ContactDataImpl;
import com.github.br.syberian.jam26.ecs.systems.physics.contact.PlayerContactDataImpl;
import com.github.br.syberian.jam26.ecs.systems.physics.contact.TriggerContactDataImpl;

import box2dLight.ConeLight;

public class EntityFactory extends BaseSystem {

    private Archetype playerArchetype;
    private Archetype hudInventoryArchetype;
    private Archetype triggerArchetype;
    private Archetype enemyArchetype;
    private Archetype npcArchetype;
    private Archetype wallArchetype;
    private Archetype bulletArchetype;
    private Archetype boxArchetype;
    private Archetype cameraArchetype;
    private Archetype doorArchetype;
    private Archetype itemArchetype;
    private Archetype interactableArchetype;

    private Archetype talkingLabelArchetype;

    private IUserActorFactory userActorFactory;

    private GameManager gameManager;
    private TriggerActionFactory triggerActionFactory;

    private Mappers mappers;

    @Override
    protected void initialize() {
        setEnabled(false);  // Отключаем ежекадровое обновление

        triggerActionFactory = getWorld().getSystem(TriggerActionFactory.class);
        mappers = getWorld().getSystem(Mappers.class);

        UtilsObjectsHolder utilsObjectsHolder = getWorld().getSystem(UtilsObjectsHolder.class);
        gameManager = utilsObjectsHolder.getGameManager();

        hudInventoryArchetype = new ArchetypeBuilder()
            .add(HudComponent.class)
            .add(RenderComponent.class)
            .build(world);

        playerArchetype = new ArchetypeBuilder()
            .add(TransformComponent.class)
            .add(PhysicsComponent.class)
            .add(PlayerComponent.class)
            .add(RenderComponent.class)
            .build(world);


        enemyArchetype = new ArchetypeBuilder()
            .add(TransformComponent.class)
            .add(PhysicsComponent.class)
            .add(EnemyComponent.class)
            .add(RenderComponent.class)
            .build(world);

        npcArchetype = new ArchetypeBuilder()
            .add(TransformComponent.class)
            .add(PhysicsComponent.class)
            .add(TriggerComponent.class)
            .add(RenderComponent.class)
            .build(world);

        wallArchetype = new ArchetypeBuilder()
            .add(TransformComponent.class)
            .add(PhysicsComponent.class)
            .add(RenderComponent.class)
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

        itemArchetype = new ArchetypeBuilder()
            .add(TransformComponent.class)
            .add(PhysicsComponent.class)
            .add(ItemComponent.class)
            .add(TriggerComponent.class)
            .add(RenderComponent.class)
            .build(world);

        interactableArchetype = new ArchetypeBuilder()
            .add(TransformComponent.class)
            .add(PhysicsComponent.class)
            .add(InteractableStateComponent.class)
            .add(TriggerComponent.class)
            .add(RenderComponent.class)
            .build(world);

        talkingLabelArchetype = new ArchetypeBuilder()
            .add(TransformComponent.class)
            .add(FollowTheTargetComponent.class)
            .add(RenderComponent.class)
            .build(world);
    }

    @Override
    protected void processSystem() {
    }

    public int createTalkingLabel(
        int targetId,
        String layerName,
        float targetHeightPixels
    ) {
        // Используем правильный архетип диалога
        int id = world.create(talkingLabelArchetype);

        // 1. Инициализируем трансформ (стартовые координаты не важны, система следования их сразу перезапишет)
        TransformComponent transformComponent = mappers.transformMapper.get(id);
        transformComponent.x = 0;
        transformComponent.y = 0;
        transformComponent.rotationDegree = 0;

        // 2. Настраиваем рендер и кастомный слой
        RenderComponent renderComponent = mappers.renderMapper.get(id);
        renderComponent.layerName = layerName;
        renderComponent.actor = userActorFactory.getCustomUiActor(ActorTypeEnum.TALKING);

        // 3. Настраиваем следование
        FollowTheTargetComponent followComp = mappers.followTheTargetMapper.get(id);
        followComp.targetId = targetId;
        // Центрируем облачко по горизонтали (если нужно) и поднимаем его над головой NPC на его высоту + небольшой запас
        followComp.offsetX = -renderComponent.actor.getWidth() / 4f;
        followComp.offsetY = targetHeightPixels + 10f; // 10 пикселей зазора над головой


        return id;
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
        EntityCreationData creationData
    ) {
        int id = world.create(triggerArchetype);

        TransformComponent transformComponent = createTransformComponent(creationData, id);

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.StaticBody, true, creationData, new TriggerContactDataImpl(id)
        );

        createTriggerComponent(triggerType, useType, conditionType, id);

        return id;
    }

    public int createInventoryHud(EntityCreationData creationData) {
        int id = world.create(hudInventoryArchetype);
        createRenderComponent(creationData, id);
        return id;
    }

    public int createPlayer(EntityCreationData creationData) {
        int id = world.create(playerArchetype);

        createTransformComponent(creationData, id);

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.DynamicBody, false, creationData, new PlayerContactDataImpl(id)
        );

        createPhysicsSensor(creationData.width, creationData.height, physicsComponent);

        PlayerComponent playerComponent = mappers.playerMapper.get(id);

        //TODO пока анимации нету
//        AnimationComponent animationComponent = animationMapper.get(id);
//        animationComponent.simpleAnimationComponent = AnimationFactory.createHero();

        RenderComponent renderComponent = createRenderComponent(creationData, id);

        return id;
    }

    private int createEnemy(EntityCreationData creationData) {
        int id = world.create(enemyArchetype);

        createTransformComponent(creationData, id);

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id,
            BodyDef.BodyType.DynamicBody,
            false,
            creationData,
            new TriggerContactDataImpl(id)
        );

        //TODO пока анимации нету
//        AnimationComponent animationComponent = animationMapper.get(id);
//        animationComponent.simpleAnimationComponent = AnimationFactory.createHero();

        RenderComponent renderComponent = createRenderComponent(creationData, id);

        return id;
    }

    private int createNpc(EntityCreationData creationData) {
        int id = world.create(npcArchetype);

        createTransformComponent(creationData, id);

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id,
            BodyDef.BodyType.DynamicBody,
            false,
            creationData,
            new TriggerContactDataImpl(id)
        );
        createPhysicsSensor(creationData.width, creationData.height, physicsComponent);

        TriggerComponent triggerComponent = createTriggerComponent(
            TriggerType.SHOW_NPC_DIALOG,
            TriggerComponent.UseType.PERSISTENCE,
            TriggerComponent.ConditionType.ON_KEY_E,
            id
        );

        //TODO пока анимации нету
//        AnimationComponent animationComponent = animationMapper.get(id);
//        animationComponent.simpleAnimationComponent = AnimationFactory.createHero();

        RenderComponent renderComponent = createRenderComponent(creationData, id);

        return id;
    }

    public int createWall(EntityCreationData creationData, boolean isVisible) {
        int id = world.create(wallArchetype);

        createTransformComponent(creationData, id);

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id, BodyDef.BodyType.StaticBody, false, creationData, new ContactDataImpl(id)
        );

        if (isVisible) {
            RenderComponent renderComponent = createRenderComponent(creationData, id);
        }

        return id;
    }

    public int createBullet(EntityCreationData creationData) {
        int id = world.create(bulletArchetype);

        createTransformComponent(creationData, id);

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id,
            BodyDef.BodyType.DynamicBody,
            false,
            creationData,
            new TriggerContactDataImpl(id)
        );

        return id;
    }

    public int createDoor(EntityCreationData creationData) {
        return createInteractableObject(
            creationData, null, TriggerType.TOGGLE_DOOR
        );
    }

    // все в пикселях приходит
    public int createCamera(EntityCreationData creationData) {
        int id = world.create(cameraArchetype);

        createTransformComponent(creationData, id);

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id,
            BodyDef.BodyType.KinematicBody,
            false,
            creationData,
            new ContactDataImpl(id)
        );

        SecurityCameraComponent cam = mappers.cameraLogicMapper.get(id);

        // --- АВТОМАТИЧЕСКИЙ РАСЧЕТ СМЕЩЕНИЯ ИЗ ФОРМЫ ТЕЛА ---
        if (physicsComponent.body != null && !physicsComponent.body.getFixtureList().isEmpty()) {
            Fixture fixture = physicsComponent.body.getFixtureList().first();
            if (fixture.getShape() instanceof PolygonShape) {
                PolygonShape shape = (PolygonShape) fixture.getShape();
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

        // Находим стартовую мировую позицию объектива
        Vector2 bodyCenterPos = physicsComponent.body.getPosition();

        // Считаем смещение с учетом стартового угла
        cam.worldLightPosMetres.set(cam.localLightOffset);
        cam.worldLightPosMetres.rotateRad(physicsComponent.body.getAngle());
        cam.worldLightPosMetres.add(bodyCenterPos);

        cam.cameraLight = new ConeLight(
            physics.getRayHandler(),
            120,
            com.badlogic.gdx.graphics.Color.YELLOW,
            cam.cameraRange,
            cam.worldLightPosMetres.x,
            cam.worldLightPosMetres.y,
            cam.viewDirectionDegrees,
            cam.fovDegrees
        );
        // Маска: все биты (0xFFFF), кроме CATEGORY_SENSOR (~CATEGORY_SENSOR)
        short lightMask = (short) ~CollisionType.CATEGORY_SENSOR;
        cam.cameraLight.setContactFilter(CollisionType.CATEGORY_DEFAULT, (short) 0, lightMask);

        RenderComponent renderComponent = createRenderComponent(creationData, id);

        return id;
    }

    public int createBox(EntityCreationData creationData) {
        int id = world.create(boxArchetype);

        createTransformComponent(creationData, id);

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id,
            BodyDef.BodyType.DynamicBody,
            false,
            creationData,
            new ContactDataImpl(id)
        );

        RenderComponent renderComponent = createRenderComponent(creationData, id);

        return id;
    }

    public int createWires(EntityCreationData creationData) {
        return createItem(creationData, TriggerType.PICK_UP_ITEM); //TODO
    }

    public int createWireCutters(EntityCreationData creationData) {
        return createItem(creationData, TriggerType.PICK_UP_ITEM);
    }

    public int createPump(EntityCreationData creationData) {
        return createItem(creationData, TriggerType.PICK_UP_ITEM);
    }

    private int createItem(EntityCreationData creationData, TriggerType triggerType) {
        int id = createDynamicObject(itemArchetype, creationData, triggerType);
        ItemComponent itemComponent = mappers.itemMapper.get(id);
        itemComponent.itemId = creationData.actorType.name();

        return id;
    }

    private int createInteractableObject(
        EntityCreationData creationData,
        ActorTypeEnum requiredItemId,
        TriggerType triggerType
    ) {
        int id = createDynamicObject(interactableArchetype, creationData, triggerType);

        InteractableStateComponent itemComponent = mappers.interactStateMapper.get(id);
        itemComponent.isOpen = false;
        itemComponent.isLocked = false;
        itemComponent.openFrameNumber = 1;
        itemComponent.closedFrameNumber = 0;
        if (requiredItemId != null) {
            itemComponent.requiredItemIdFromPayerInventory = requiredItemId.name();
        }

        return id;
    }

    private int createDynamicObject(
        Archetype archetype,
        EntityCreationData creationData,
        TriggerType triggerType
    ) {
        int id = world.create(archetype);

        createTransformComponent(creationData, id);

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id,
            BodyDef.BodyType.KinematicBody,
            false,
            creationData,
            new TriggerContactDataImpl(id)
        );
        createPhysicsSensor(creationData.width, creationData.height, physicsComponent);

        TriggerComponent triggerComponent = createTriggerComponent(
            triggerType,
            TriggerComponent.UseType.PERSISTENCE,
            TriggerComponent.ConditionType.ON_KEY_E,
            id
        );

        RenderComponent renderComponent = createRenderComponent(creationData, id);

        return id;
    }

    public int createMed(EntityCreationData creationData) {
        return createItem(creationData, TriggerType.TOGGLE_CONTAINER);
    }

    public int createIronCabinet(EntityCreationData creationData) {
        return createItem(creationData, TriggerType.TOGGLE_CONTAINER);
    }

    private int createWeightPlate(EntityCreationData creationData) {
        return createItem(creationData, TriggerType.PICK_UP_ITEM);
    }

    public int createBoxHolder(EntityCreationData creationData) {
        return createItem(creationData, TriggerType.PICK_UP_ITEM);
    }

    private int createLever(EntityCreationData creationData) {
        return createItem(creationData, TriggerType.TOGGLE_CONTAINER);
    }

    private int createKeyHolder(EntityCreationData creationData) {
        return createItem(creationData, TriggerType.TOGGLE_CONTAINER);
    }

    private int createBed(EntityCreationData creationData) {
        int id = world.create(boxArchetype);

        createTransformComponent(creationData, id);

        PhysicsComponent physicsComponent = createPhysicsComponent(
            id,
            BodyDef.BodyType.KinematicBody,
            false,
            creationData,
            new TriggerContactDataImpl(id)
        );

        RenderComponent renderComponent = createRenderComponent(creationData, id);

        return id;
    }

    public int createBedWhite(EntityCreationData creationData) {
        return createBed(creationData);
    }

    public int createBedGreen(EntityCreationData creationData) {
        return createBed(creationData);
    }

    private PhysicsComponent createPhysicsComponent(
        int entityId,
        BodyDef.BodyType bodyType,
        boolean isSensor,
        EntityCreationData creationData,
        ContactData contactData
    ) {
        SimpleUtils simpleUtils = gameManager.simpleUtils;

        // 1. Вычисляем половины размеров, так как setAsBox принимает полупериметр
        float halfWidth = creationData.width / 2f;
        float halfHeight = creationData.height / 2f;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;

        float angleRad = creationData.rotation * MathUtils.degreesToRadians;
        float deltaX = creationData.width / 2f * MathUtils.cos(angleRad) - creationData.height / 2f * MathUtils.sin(angleRad);
        float deltaY = creationData.width / 2f * MathUtils.sin(angleRad) + creationData.height / 2f * MathUtils.cos(angleRad);
        // 2. Смещаем точку спавна из левого нижнего угла в геометрический центр объекта
        bodyDef.position.set(
            simpleUtils.convertPixelsToMetres(creationData.x + deltaX),
            simpleUtils.convertPixelsToMetres(creationData.y + deltaY)
        );
        bodyDef.angle = angleRad;

        PhysicsSystem physics = getWorld().getSystem(PhysicsSystem.class);
        World box2dWorld = physics.getBox2dWorld();
        Body body = box2dWorld.createBody(bodyDef);

        PolygonShape groundBox = new PolygonShape();
        // 3. Передаем именно половины ширины и высоты
        groundBox.setAsBox(
            simpleUtils.convertPixelsToMetres(halfWidth),
            simpleUtils.convertPixelsToMetres(halfHeight)
        );

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.isSensor = isSensor;
        if (isSensor) {
            fixtureDef.filter.categoryBits = CollisionType.CATEGORY_SENSOR;
        }
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
        body.setUserData(contactData);

        PhysicsComponent physicsComponent = mappers.physicsMapper.get(entityId);
        physicsComponent.body = body;

        return physicsComponent;
    }

    public void setUserActorFactory(IUserActorFactory userActorFactory) {
        this.userActorFactory = userActorFactory;
    }

    public Actor createEntityFromUI(EntityCreationData creationData) {
        MapObject mapObject = creationData.mapObject;
        ActorTypeEnum actorType = creationData.actorType;

        MapProperties properties = mapObject.getProperties();

        int entityId;
        switch (actorType) {
            case INVENTORY_HUD:
                entityId = createInventoryHud(creationData);
                break;
            case HERO:
            case CHAR:
            case CHAR_1:
                entityId = createPlayer(creationData);
                break;
            case NPC:
                entityId = createNpc(creationData);
                break;
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

                entityId = createTrigger(triggerTypeEnum, useTypeEnum, conditionTypeEnum, creationData);
                break;
            case ENEMY:
                entityId = createEnemy(creationData);
                break;
            case BOX_OPEN:
            case KOROB_OPEN:
                entityId = createBox(creationData);
                break;
            case BOX:
            case KOROB:
                entityId = createBox(creationData);
                break;
            case CAMERA:
            case CAM_1:
                entityId = createCamera(creationData);
                break;
            case CAMERA_2:
            case CAM_2: // TODO это камера, которая смотрит прямо на игрока
                entityId = createCamera(creationData);
                break;
            case BULLET:
                entityId = createBullet(creationData);
                break;
            case WALL:
                entityId = createWall(creationData, false);
                break;
            case DOOR:
            case DOOR_BROWN:
            case DOOR_GRAY:
                entityId = createDoor(creationData);
                break;
            case WIRES:
                entityId = createWires(creationData);
                break;
            case WIRE_CUTTERS:
                entityId = createWireCutters(creationData);
                break;
            case PUMPKIN:
            case PUMP:
                entityId = createPump(creationData);
                break;
            case WEIGHT_PLATE:
                entityId = createWeightPlate(creationData);
                break;
            case TOILET:
                entityId = createWall(creationData, true);
                break;
            case SINK:
                entityId = createWall(creationData, true);
                break;
            case TABLE:
                entityId = createBox(creationData);
                break;
            case CHAIR:
                entityId = createBox(creationData);
                break;
            case BED_WHITE:
                entityId = createBedWhite(creationData);
                break;
            case BED_GREEN:
                entityId = createBedGreen(creationData);
                break;
            case COOLER:
                entityId = createBox(creationData);
                break;
            case SPORT_BENCH:
            case FID_SPORT_BENCH:
            case DINING_BENCH:
                entityId = createWall(creationData, true);
                break;
            case CUP:
                entityId = createBox(creationData);
                break;
            case MED:
                entityId = createMed(creationData);
                break;
            case IRON_CABINET:
                entityId = createIronCabinet(creationData);
                break;
            case CART:
                entityId = createIronCabinet(creationData);
                break;
            case SECURITY_MONITORS:
                entityId = createWall(creationData, true);
                break;
            case GATE:
                entityId = createWall(creationData, true);
                break;
            case BARBELL:
                entityId = createWall(creationData, true);
                break;
            case BOX_HOLDER:
                entityId = createBoxHolder(creationData);
                break;
            case LEVER:
                entityId = createLever(creationData);
                break;
            case KEY:
                entityId = createKeyHolder(creationData);
                break;
            default:
                throw new GdxRuntimeException("createEntityFromUI: " + actorType + " is not found");
        }

        String tag = creationData.tag;
        if (tag != null) {
            TagManager tagManager = world.getSystem(TagManager.class);
            // Защита от дубликатов: в Artemis один тег может принадлежать только ОДНОЙ сущности.
            // Если на карте случайно назвали два объекта одинаково, выкидываем понятную ошибку.
            if (tagManager.getEntityId(tag) != -1) {
                throw new GdxRuntimeException("Entity with tag [" + tag + "] have already registered in TagManager");
            }

            tagManager.register(tag, entityId);
        }

        return getActor(actorType, entityId);
    }

    private @Null Actor getActor(ActorTypeEnum actorType, int entityId) {
        ComponentMapper<RenderComponent> mapper = world.getMapper(RenderComponent.class);
        RenderComponent renderComponent = mapper.get(entityId);
        if (renderComponent == null || renderComponent.actor == null) {
            Gdx.app.log("WARN", "UserActorFactory#createEcsEntity, actorTypeEnum=" +
                actorType + " render component is null");
            return null;
        }

        return renderComponent.actor;
    }

    private TransformComponent createTransformComponent(EntityCreationData creationData, int id) {
        TransformComponent transformComponent = mappers.transformMapper.get(id);
        transformComponent.x = creationData.x;
        transformComponent.y = creationData.y;
        transformComponent.rotationDegree = creationData.rotation;

        return transformComponent;
    }

    private void createPhysicsSensor(
        float width,
        float height,
        PhysicsComponent physicsComponent
    ) {
        SimpleUtils simpleUtils = gameManager.simpleUtils;
        // сенсор для столкновений и триггеров

        Body body = physicsComponent.body;
        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(
            simpleUtils.convertPixelsToMetres(width / 2 * 1.3f),
            simpleUtils.convertPixelsToMetres(height / 2 * 1.3f)
        );

        FixtureDef sensorDef = new FixtureDef();
        sensorDef.shape = sensorShape;
        sensorDef.isSensor = true;
        sensorDef.filter.categoryBits = CollisionType.CATEGORY_SENSOR;

        Fixture sensorFixture = body.createFixture(sensorDef);

        sensorShape.dispose();
    }

    private TriggerComponent createTriggerComponent(
        TriggerType triggerType,
        TriggerComponent.UseType useType,
        TriggerComponent.ConditionType conditionType,
        int entityId
    ) {
        TriggerComponent trigger = mappers.triggerMapper.get(entityId);
        trigger.conditionType = conditionType;
        trigger.useType = useType;

        // Передаем кастомный код текстом/логикой:
        TriggerActionFactory triggerActionFactory = getWorld().getSystem(TriggerActionFactory.class);
        trigger.action = triggerActionFactory.create(triggerType);

        return trigger;
    }

    private RenderComponent createRenderComponent(EntityCreationData creationData, int id) {
        RenderComponent renderComponent = mappers.renderMapper.get(id);
        renderComponent.actor = userActorFactory.getCustomUiActor(creationData.actorType);
        renderComponent.layerName = creationData.layerName;

        return renderComponent;
    }

}
