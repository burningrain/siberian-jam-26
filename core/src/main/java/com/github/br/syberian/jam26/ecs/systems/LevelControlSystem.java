package com.github.br.syberian.jam26.ecs.systems;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.utils.IntBag;
import com.github.br.syberian.jam26.TriggerActionFactory;
import com.github.br.syberian.jam26.TriggerType;
import com.github.br.syberian.jam26.ecs.components.SecurityCameraComponent;
import com.github.br.syberian.jam26.ecs.components.TriggerAction;

public class LevelControlSystem extends BaseSystem {

    private ComponentMapper<SecurityCameraComponent> mCamera;
    private EntitySubscription cameraSubscription;

    // Состояние проигрыша
    private boolean isGameOver = false;

    public LevelControlSystem() {
    }

    @Override
    protected void initialize() {
        mCamera = world.getMapper(SecurityCameraComponent.class);
        cameraSubscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(SecurityCameraComponent.class));
    }

    @Override
    protected void processSystem() {
        if (isGameOver) {
            restartLevel();
            return;
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
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    private void restartLevel() {
        TriggerActionFactory triggerActionFactory = world.getSystem(TriggerActionFactory.class);
        TriggerAction restartAction = triggerActionFactory.create(TriggerType.RESTART_LEVEL); // Или твой тип триггера
        restartAction.execute(getWorld(), -1, -1);
    }

}
