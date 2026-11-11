package com.github.br.syberian.jam26;

import com.artemis.BaseSystem;
import com.artemis.World;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.br.libgdx.common.structure.GameManager;
import com.github.br.libgdx.common.structure.screen.RestartScreen;
import com.github.br.libgdx.common.structure.screen.ui.AnimatedImage;
import com.github.br.syberian.jam26.ecs.components.CutsceneComponent;
import com.github.br.syberian.jam26.ecs.components.DeleteIntentComponent;
import com.github.br.syberian.jam26.ecs.components.InteractableStateComponent;
import com.github.br.syberian.jam26.ecs.components.ItemComponent;
import com.github.br.syberian.jam26.ecs.components.PhysicsComponent;
import com.github.br.syberian.jam26.ecs.components.PlayerComponent;
import com.github.br.syberian.jam26.ecs.components.RenderComponent;
import com.github.br.syberian.jam26.ecs.components.TriggerAction;
import com.github.br.syberian.jam26.ecs.systems.UtilsObjectsHolder;
import com.github.br.syberian.jam26.ecs.systems.cutscene.step.MoveToStep;
import com.github.br.syberian.jam26.ecs.systems.cutscene.step.SayStep;
import com.github.br.syberian.jam26.ecs.systems.cutscene.step.WaitStep;
import com.github.br.syberian.jam26.ecs.systems.physics.CollisionType;
import com.github.br.syberian.jam26.ecs.systems.renderer.Box2dRenderSystem;

public class TriggerActionFactory extends BaseSystem {

    public final TriggerAction RESTART_LEVEL = createRestartLevelAction();
    public final TriggerAction SHOW_MESSAGE = createShowMessageAction();
    public final TriggerAction PICK_UP_ITEM = createPickUpItemAction();
    public final TriggerAction EXIT_TRIGGER = createTriggerExitAction();
    public final TriggerAction TOGGLE_DOOR = createToggleDoorAction();
    public final TriggerAction TOGGLE_CONTAINER = createToggleContainerAction();

    private final RestartScreen currentScreen;

    private Mappers mappers;

    private GameManager gameManager;

    public TriggerActionFactory(RestartScreen currentScreen) {
        this.currentScreen = currentScreen;
    }

    @Override
    protected void initialize() {
        mappers = getWorld().getSystem(Mappers.class);

        UtilsObjectsHolder utilsObjectsHolder = getWorld().getSystem(UtilsObjectsHolder.class);
        gameManager = utilsObjectsHolder.getGameManager();
    }

    @Override
    protected void processSystem() {
    }

    public TriggerAction create(TriggerType triggerType) {
        switch (triggerType) {
            case RESTART_LEVEL:
                return this.RESTART_LEVEL;

            case SHOW_MESSAGE:
                return this.SHOW_MESSAGE;
            case PICK_UP_ITEM:
                return this.PICK_UP_ITEM;
            case EXIT_TRIGGER:
                return this.EXIT_TRIGGER;
            case TOGGLE_DOOR:
                return this.TOGGLE_DOOR;
            case TOGGLE_CONTAINER:
                return this.TOGGLE_CONTAINER;

            default:
                throw new GdxRuntimeException("TriggerActionFactory#create: [" +
                    triggerType + "] is not implemented yet");
        }
    }

    private TriggerAction createToggleContainerAction() {
        return new TriggerAction() {
            @Override
            public void execute(World world, int triggerEntityId, int activatorEntityId) {

            }
        };
    }

    private TriggerAction createPickUpItemAction() {
        return new TriggerAction() {
            @Override
            public void execute(World world, int triggerEntityId, int activatorEntityId) {
                PlayerComponent player = mappers.playerMapper.get(activatorEntityId);
                ItemComponent item = mappers.itemMapper.get(triggerEntityId);

                if (player == null || item == null) return;

                // кладем в инвентарь
                if (player.inventoryData.addItem(item.itemId)) {
                    world.getMapper(DeleteIntentComponent.class).create(triggerEntityId);
                }
            }
        };
    }

    private TriggerAction createToggleDoorAction() {
        return new TriggerAction() {
            @Override
            public void execute(World world, int triggerEntityId, int activatorEntityId) {
                InteractableStateComponent interactComponent = mappers.interactStateMapper.get(triggerEntityId);
                interactComponent.isOpen = !interactComponent.isOpen;

                PhysicsComponent physicsComponent = mappers.physicsMapper.get(triggerEntityId);
                RenderComponent renderComponent = mappers.renderMapper.get(triggerEntityId);
                AnimatedImage animatedImage = (AnimatedImage) renderComponent.actor;

                int frameNumber = interactComponent.isOpen ?
                    interactComponent.openFrameNumber : interactComponent.closedFrameNumber;
                animatedImage.setFrameAndPause(frameNumber);

                Body body = physicsComponent.body;
                float oldWidth = animatedImage.getWidth();
                float newWidth = interactComponent.isOpen ? oldWidth / 2f : oldWidth * 2f;
                animatedImage.setWidth(newWidth);

                // 1. Считаем, на сколько сдвинулся центр объекта вдоль его локальной оси X
                // Если уменьшаем ширину (открываем) — центр едет вправо (+). Если увеличиваем — влево (-).
                float deltaCenterPixels = (oldWidth - newWidth) / 2f;
                float deltaCenterMetres = gameManager.simpleUtils.convertPixelsToMetres(deltaCenterPixels);

                // 2. Сдвигаем тело Box2D с учетом его угла поворота (rotation)
                float angle = body.getAngle();
                Vector2 pos = body.getPosition();
                float newPosX = pos.x + deltaCenterMetres * MathUtils.cos(angle);
                float newPosY = pos.y + deltaCenterMetres * MathUtils.sin(angle);
                body.setTransform(newPosX, newPosY, angle);

                // 3. Обновляем полу-ширину основной фикстуры (чтобы PhysicsSystem брал новый halfWidthPixels)
                Fixture mainFixture = body.getFixtureList().first();
                if (mainFixture.getShape() instanceof PolygonShape) {
                    PolygonShape shape = (PolygonShape) mainFixture.getShape();
                    Vector2 tmpVertex = new Vector2();
                    shape.getVertex(2, tmpVertex); // берем текущую halfHeight в y
                    float newHalfWidthMetres = gameManager.simpleUtils.convertPixelsToMetres(newWidth / 2f);
                    shape.setAsBox(newHalfWidthMetres, tmpVertex.y);
                }

                // ВАЖНО: не вызывайте body.setActive(false), иначе триггер на этом теле перестанет ловить нажатие E для закрытия!
                // Вместо этого делайте твердую фикстуру сенсором:
                mainFixture.setSensor(interactComponent.isOpen);
                Filter filter = mainFixture.getFilterData();
                filter.categoryBits = interactComponent.isOpen?
                    CollisionType.CATEGORY_SENSOR : CollisionType.CATEGORY_DEFAULT;
                mainFixture.setFilterData(filter);
            }
        };
    }

    private TriggerAction createTriggerExitAction() {
        return new TriggerAction() {
            @Override
            public void execute(World world, int triggerEntityId, int activatorEntityId) {
                //fixme нужно нормально честно удалять объекты, чтобы они не попадали на отрисовку
                world.getSystem(Box2dRenderSystem.class).setEnabled(false);
                currentScreen.triggerExit();
            }
        };
    }

    private TriggerAction createRestartLevelAction() {
        return new TriggerAction() {
            @Override
            public void execute(World world, int triggerEntityId, int activatorEntityId) {
                //fixme нужно нормально честно удалять объекты, чтобы они не попадали на отрисовку
                world.getSystem(Box2dRenderSystem.class).setEnabled(false);
                currentScreen.restart();
            }
        };
    }

    private TriggerAction createShowMessageAction() {
        return new TriggerAction() {
            @Override
            public void execute(World world, int triggerEntityId, int activatorEntityId) {
                // 1. Блокируем ввод игрока
                GamePlaySettingsImpl gamePlaySettings = (GamePlaySettingsImpl) gameManager.gameSettings.getGamePlaySettings();
                gamePlaySettings.setDialogueActive(true);

                // 2. Создаем динамическую сущность режиссера сценария
                int cutsceneEntityId = world.create();
                CutsceneComponent cutscene = world.getMapper(CutsceneComponent.class).create(cutsceneEntityId);

                // Достаем ID нужного NPC (например, из свойств триггера или подписки)
                int npcEntityId = triggerEntityId;

                // 3. РЕЖИССУРА: Набиваем сценарий по порядку выполнения!
                cutscene.steps.add(new SayStep(npcEntityId, "Стоять! Ты кто такой?!"));
                cutscene.steps.add(new SayStep(activatorEntityId, "Я просто мимо проходил..."));
                cutscene.steps.add(new WaitStep(1.0f)); // Драматическая пауза в секунду
                cutscene.steps.add(new SayStep(npcEntityId, "Сюда иди, разбираться будем!"));
                cutscene.steps.add(new MoveToStep(npcEntityId, 500f, 300f)); // Идет в координаты комнаты
                cutscene.steps.add(new SayStep(npcEntityId, "Давай быстрее."));
            }
        };
    }

}
