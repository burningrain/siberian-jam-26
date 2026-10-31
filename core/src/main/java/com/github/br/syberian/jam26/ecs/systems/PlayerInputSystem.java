package com.github.br.syberian.jam26.ecs.systems;

import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.github.br.syberian.jam26.ecs.components.PhysicsComponent;
import com.github.br.syberian.jam26.ecs.components.PlayerComponent;

@All({PlayerComponent.class, PhysicsComponent.class})
public class PlayerInputSystem extends IteratingSystem {

    @Override
    protected void process(int entityId) {

    }

}
