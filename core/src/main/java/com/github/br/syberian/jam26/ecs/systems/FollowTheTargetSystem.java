package com.github.br.syberian.jam26.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.github.br.syberian.jam26.ecs.components.FollowTheTargetComponent;
import com.github.br.syberian.jam26.ecs.components.TransformComponent;

@All({FollowTheTargetComponent.class, TransformComponent.class})
public class FollowTheTargetSystem extends IteratingSystem {

    protected ComponentMapper<FollowTheTargetComponent> mFollow;
    protected ComponentMapper<TransformComponent> mTransform;

    @Override
    protected void initialize() {
        this.mTransform = getWorld().getMapper(TransformComponent.class);
        this.mFollow = getWorld().getMapper(FollowTheTargetComponent.class);
    }

    @Override
    protected void process(int entityId) {
        FollowTheTargetComponent follow = mFollow.get(entityId);
        TransformComponent followTransform = mTransform.get(entityId);

        // Проверяем, существует ли еще цель в мире (например, если NPC внезапно удалили)
        if (!getWorld().getEntityManager().isActive(follow.targetId)) {
            // Если цель мертва, удаляем облачко диалога
            getWorld().delete(entityId);
            return;
        }

        TransformComponent targetTransform = mTransform.get(follow.targetId);
        if (targetTransform == null) {
            return;
        }

        // Копируем координаты цели и добавляем смещение (чтобы висело точно над головой)
        followTransform.x = targetTransform.x + follow.offsetX;
        followTransform.y = targetTransform.y + follow.offsetY;

        // Угол облачка обычно оставляем нулевым, чтобы текст не крутился, если NPC повернется
        followTransform.rotationDegree = 0;
    }

}
