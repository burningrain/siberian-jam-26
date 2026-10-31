package com.github.br.syberian.jam26.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.github.br.syberian.jam26.ecs.components.DeleteIntentComponent;
import com.github.br.syberian.jam26.ecs.components.TriggerComponent;
import com.github.br.syberian.jam26.ecs.components.TriggerContactComponent;
import com.github.br.syberian.jam26.ecs.components.TriggerCooldownComponent;

@All({TriggerComponent.class})
public class TriggerSystem extends IteratingSystem {

    protected ComponentMapper<TriggerComponent> mTrigger;
    protected ComponentMapper<TriggerContactComponent> mContact;
    protected ComponentMapper<TriggerCooldownComponent> mCooldown;
    protected ComponentMapper<DeleteIntentComponent> mDelete;

    @Override
    protected void initialize() {
        this.mTrigger = getWorld().getMapper(TriggerComponent.class);
        this.mContact = getWorld().getMapper(TriggerContactComponent.class);
        this.mDelete = getWorld().getMapper(DeleteIntentComponent.class);
        this.mCooldown = getWorld().getMapper(TriggerCooldownComponent.class);
    }

    @Override
    protected void process(int triggerEntityId) {
        TriggerComponent trigger = mTrigger.get(triggerEntityId);
        boolean hasContact = mContact.has(triggerEntityId);
        boolean hasCooldown = mCooldown.has(triggerEntityId);

        // -----------------------------------------------------------------
        // ЛОГИКА АКТИВАЦИИ (Игрок внутри триггера)
        // -----------------------------------------------------------------
        if (hasContact) {
            TriggerContactComponent contact = mContact.get(triggerEntityId);

            // Проверяем условия ввода
            boolean conditionMet = false;
            if (trigger.conditionType == TriggerComponent.ConditionType.ON_ENTER) {
                conditionMet = true;
            } else if (trigger.conditionType == TriggerComponent.ConditionType.ON_KEY_E) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    conditionMet = true;
                }
            }

            // Главная проверка: если условие выполнено и триггер НЕ на перезарядке
            if (conditionMet && !hasCooldown && trigger.action != null) {

                // Выполняем кастомный код
                trigger.action.execute(getWorld(), triggerEntityId, contact.activatorEntityId);

                // Обрабатываем типы триггеров после успешной активации
                if (trigger.useType == TriggerComponent.UseType.ONE_TIME) {
                    mDelete.create(triggerEntityId); // Удаляем навсегда
                }
                else if (trigger.useType == TriggerComponent.UseType.ON_ENTER_ONCE) {
                    mCooldown.create(triggerEntityId); // Блокируем триггер до тех пор, пока игрок внутри
                }
            }
        }
        // -----------------------------------------------------------------
        // ЛОГИКА СБРОСА (Игрок вышел из триггера)
        // -----------------------------------------------------------------
        else {
            // Если контакта с игроком больше нет, но висит кулдаун — сбрасываем его
            if (hasCooldown) {
                mCooldown.remove(triggerEntityId);
            }
        }
    }
}
