package com.github.br.syberian.jam26;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.github.br.syberian.jam26.ecs.components.EnemyComponent;
import com.github.br.syberian.jam26.ecs.components.FollowTheTargetComponent;
import com.github.br.syberian.jam26.ecs.components.InteractableStateComponent;
import com.github.br.syberian.jam26.ecs.components.ItemComponent;
import com.github.br.syberian.jam26.ecs.components.PhysicsComponent;
import com.github.br.syberian.jam26.ecs.components.PlayerComponent;
import com.github.br.syberian.jam26.ecs.components.RenderComponent;
import com.github.br.syberian.jam26.ecs.components.SecurityCameraComponent;
import com.github.br.syberian.jam26.ecs.components.TransformComponent;
import com.github.br.syberian.jam26.ecs.components.TriggerComponent;

public class Mappers extends BaseSystem {

    public ComponentMapper<TransformComponent> transformMapper;
    public ComponentMapper<PhysicsComponent> physicsMapper;
    public ComponentMapper<RenderComponent> renderMapper;

    public ComponentMapper<PlayerComponent> playerMapper;
    public ComponentMapper<EnemyComponent> enemyMapper;

    public ComponentMapper<TriggerComponent> triggerMapper;

    public ComponentMapper<SecurityCameraComponent> cameraLogicMapper;

    public ComponentMapper<ItemComponent> itemMapper;

    public ComponentMapper<InteractableStateComponent> interactStateMapper;
    public ComponentMapper<FollowTheTargetComponent> followTheTargetMapper;

    @Override
    protected void processSystem() {
    }

    @Override
    protected void initialize() {
        setEnabled(false);  // Отключаем ежекадровое обновление

        transformMapper = getWorld().getMapper(TransformComponent.class);
        physicsMapper = getWorld().getMapper(PhysicsComponent.class);
        renderMapper = getWorld().getMapper(RenderComponent.class);
        playerMapper = getWorld().getMapper(PlayerComponent.class);
        enemyMapper = getWorld().getMapper(EnemyComponent.class);
        triggerMapper = getWorld().getMapper(TriggerComponent.class);
        cameraLogicMapper = getWorld().getMapper(SecurityCameraComponent.class);
        itemMapper = getWorld().getMapper(ItemComponent.class);
        interactStateMapper = getWorld().getMapper(InteractableStateComponent.class);
        followTheTargetMapper = getWorld().getMapper(FollowTheTargetComponent.class);
    }

}
