package com.github.br.syberian.jam26.ecs.systems.cutscene;

import com.artemis.World;

public interface CutsceneStep {

    /**
     * Вызывается один раз, когда шаг становится активным.
     */
    void start(World world);

    /**
     * Вызывается каждый кадр.
     * @return true, если шаг ЗАВЕРШЕН и можно переходить к следующему.
     */
    boolean update(World world, float delta);

    /**
     * Вызывается один раз, когда шаг завершается (для зачистки ресурсов).
     */
    void end(World world);

}
