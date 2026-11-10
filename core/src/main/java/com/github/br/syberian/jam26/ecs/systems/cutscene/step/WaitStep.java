package com.github.br.syberian.jam26.ecs.systems.cutscene.step;

import com.artemis.World;
import com.github.br.syberian.jam26.ecs.systems.cutscene.CutsceneStep;

public class WaitStep implements CutsceneStep {
    private float timeLeft;

    public WaitStep(float seconds) {
        this.timeLeft = seconds;
    }

    @Override
    public void start(World world) {
    }

    @Override
    public boolean update(World world, float delta) {
        timeLeft -= delta;
        return timeLeft <= 0;
    }

    @Override
    public void end(World world) {
    }

}
