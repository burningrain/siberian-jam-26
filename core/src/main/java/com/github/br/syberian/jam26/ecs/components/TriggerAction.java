package com.github.br.syberian.jam26.ecs.components;

import com.artemis.World;


@FunctionalInterface
public interface TriggerAction {

    /**
     * @param world Ссылка на ECS мир
     * @param triggerEntityId ID сущности самого триггера
     * @param activatorEntityId ID сущности, которая активировала триггер (игрок)
     */
    void execute(World world, int triggerEntityId, int activatorEntityId);

}
