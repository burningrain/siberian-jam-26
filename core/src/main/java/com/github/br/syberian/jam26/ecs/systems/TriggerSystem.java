package com.github.br.syberian.jam26.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.github.br.syberian.jam26.ecs.components.DeleteIntentComponent;
import com.github.br.syberian.jam26.ecs.components.TriggerComponent;
import com.github.br.syberian.jam26.ecs.components.TriggerContactComponent;

@All({TriggerComponent.class, TriggerContactComponent.class})
public class TriggerSystem extends IteratingSystem {

    protected ComponentMapper<TriggerComponent> mTrigger;
    protected ComponentMapper<TriggerContactComponent> mContact;
    protected ComponentMapper<DeleteIntentComponent> mDelete;

    @Override
    protected void initialize() {
        this.mTrigger = getWorld().getMapper(TriggerComponent.class);
        this.mContact = getWorld().getMapper(TriggerContactComponent.class);
        this.mDelete = getWorld().getMapper(DeleteIntentComponent.class);
    }

    @Override
    protected void process(int triggerEntityId) {
        TriggerComponent trigger = mTrigger.get(triggerEntityId);
        TriggerContactComponent contact = mContact.get(triggerEntityId);

        boolean shouldActivate = false;

        // Условие А: Достаточно просто наступить
        if (trigger.conditionType == TriggerComponent.ConditionType.ON_ENTER) {
            shouldActivate = true;
        }
        // Условие Б: Нажали кнопку "E", находясь внутри зоны
        else if (trigger.conditionType == TriggerComponent.ConditionType.ON_KEY_E) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                shouldActivate = true;
            }
        }

        if (shouldActivate && trigger.action != null) {
            // Запускаем кастомный код, переданный в триггер!
            trigger.action.execute(getWorld(), triggerEntityId, contact.activatorEntityId);

            // Обработка одноразовости
            if (trigger.useType == TriggerComponent.UseType.ONE_TIME) {
                // Вешаем ваш DeleteIntentComponent.
                // Ваша PhysicsSystem на следующем шаге сама безопасно очистит Box2D тело и удалит сущность.
                mDelete.create(triggerEntityId);
            }
        }
    }
}
