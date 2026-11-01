package com.github.br.syberian.jam26.ecs.systems;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.Aspect;
import com.artemis.EntitySubscription;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.github.br.libgdx.common.structure.GameManager;
import com.github.br.syberian.jam26.TriggerActionFactory;
import com.github.br.syberian.jam26.TriggerType;
import com.github.br.syberian.jam26.ecs.components.SecurityCameraComponent;
import com.github.br.syberian.jam26.ecs.components.TriggerAction;

public class LevelControlSystem extends BaseSystem {

    private final GameManager gameManager;
    private ComponentMapper<SecurityCameraComponent> mCamera;
    private EntitySubscription cameraSubscription;

    // Состояние проигрыша
    private boolean isGameOver = false;

    public LevelControlSystem(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    protected void initialize() {
        mCamera = world.getMapper(SecurityCameraComponent.class);
        // Подписываемся на все камеры, чтобы проверять, заметили ли они игрока
        cameraSubscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(SecurityCameraComponent.class));
    }

    @Override
    protected void processSystem() {
        // 1. ЕСЛИ ИГРА УЖЕ ОКОНЧЕНА: Ждем нажатия любой клавиши для рестарта
        if (isGameOver) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ANY_KEY)) {
                restartLevel();
            }
            return; // Замораживаем дальнейшую проверку камер
        }

        // 2. ЕСЛИ ИГРА ИДЕТ: Проверяем, не поймала ли какая-нибудь камера игрока
        IntBag cameras = cameraSubscription.getEntities();
        for (int i = 0; i < cameras.size(); i++) {
            int camId = cameras.get(i);
            SecurityCameraComponent camera = mCamera.get(camId);

            if (camera != null && camera.isPlayerSpotted) {
                triggerGameOver();
                break; // Одного обнаружения достаточно
            }
        }
    }

    private void triggerGameOver() {
        isGameOver = true;

        // Показываем надпись над TiledMap.
        // Если у тебя в GameManager есть UI менеджер, выводим текст там.
        // Для джема можно быстро вывести через логи или твой кастомный UI:
        System.out.println("ВЫ ОБНАРУЖЕНЫ! Нажмите любую клавишу для рестарта...");

        // TODO: Тут задействуй свой UI слой:
        // gameManager.getUiStage().showGameOverLabel("Нажмите любую клавишу");

        // Опционально: можно отключить систему ввода игрока, чтобы он замер
        PlayerInputSystem inputSystem = world.getSystem(PlayerInputSystem.class);
        if (inputSystem != null) {
            inputSystem.setEnabled(false);
        }
    }

    private void restartLevel() {
        TriggerActionFactory triggerActionFactory = world.getSystem(TriggerActionFactory.class);
        TriggerAction restartAction = triggerActionFactory.create(TriggerType.RESTART_LEVEL); // Или твой тип триггера
        restartAction.execute(getWorld(), -1, -1);
    }

}
