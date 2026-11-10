package com.github.br.syberian.jam26.ecs.systems.cutscene;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.github.br.libgdx.common.structure.GameManager;
import com.github.br.syberian.jam26.GamePlaySettingsImpl;
import com.github.br.syberian.jam26.ecs.components.CutsceneComponent;
import com.github.br.syberian.jam26.ecs.systems.UtilsObjectsHolder;

@All(CutsceneComponent.class)
public class CutsceneSystem extends IteratingSystem {

    protected ComponentMapper<CutsceneComponent> mCutscene;
    protected GameManager gameManager;

    @Override
    protected void initialize() {
        mCutscene = getWorld().getMapper(CutsceneComponent.class);
        gameManager = getWorld().getSystem(UtilsObjectsHolder.class).getGameManager();
    }

    @Override
    protected void process(int entityId) {
        CutsceneComponent cutscene = mCutscene.get(entityId);

        // Если все шаги пройдены — уничтожаем сущность режиссера
        if (cutscene.currentStepIndex >= cutscene.steps.size) {
            // Здесь можно вернуть игроку управление (разблокировать PlayerInputSystem)
            GamePlaySettingsImpl gamePlaySettings = (GamePlaySettingsImpl) gameManager.gameSettings.getGamePlaySettings();
            gamePlaySettings.setDialogueActive(false);
            getWorld().delete(entityId);
            return;
        }

        CutsceneStep currentStep = cutscene.steps.get(cutscene.currentStepIndex);

        // Запускаем шаг, если он только начался
        if (!cutscene.isInitialized) {
            currentStep.start(getWorld());
            cutscene.isInitialized = true;
        }

        // Обновляем шаг каждый кадр. Если он вернул true — завершаем его
        if (currentStep.update(getWorld(), getWorld().getDelta())) {
            currentStep.end(getWorld());
            cutscene.currentStepIndex++; // Переходим к следующему режиссёрскому шагу
            cutscene.isInitialized = false; // Сбрасываем флаг для следующего шага
        }
    }
}
