package com.github.br.syberian.jam26.ecs.systems.cutscene.step;

import com.artemis.World;
import com.badlogic.gdx.math.Vector2;
import com.github.br.syberian.jam26.ecs.components.PhysicsComponent;
import com.github.br.syberian.jam26.ecs.components.TransformComponent;
import com.github.br.syberian.jam26.ecs.systems.cutscene.CutsceneStep;

public class MoveToStep implements CutsceneStep {

    private final int entityId;
    private final Vector2 targetPosPixels;
    private final float speedMetres = 3.0f;

    public MoveToStep(int entityId, float targetX, float targetY) {
        this.entityId = entityId;
        this.targetPosPixels = new Vector2(targetX, targetY);
    }

    @Override
    public void start(World world) {}

    @Override
    public boolean update(World world, float delta) {
        TransformComponent transform = world.getMapper(TransformComponent.class).get(entityId);
        PhysicsComponent physics = world.getMapper(PhysicsComponent.class).get(entityId);

        if (transform == null || physics == null || physics.body == null) return true;

        Vector2 currentPos = new Vector2(transform.x, transform.y);
        float distance = currentPos.dst(targetPosPixels);

        // Если подошли близко (меньше 5 пикселей), останавливаем тело и завершаем шаг
        if (distance < 5f) {
            physics.body.setLinearVelocity(Vector2.Zero);
            return true;
        }

        // Считаем вектор направления движения
        Vector2 direction = new Vector2(targetPosPixels).sub(currentPos).nor().scl(speedMetres);
        physics.body.setLinearVelocity(direction);
        return false;
    }

    @Override
    public void end(World world) {}
}
