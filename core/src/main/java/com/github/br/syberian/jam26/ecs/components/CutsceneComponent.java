package com.github.br.syberian.jam26.ecs.components;

import com.artemis.Component;
import com.badlogic.gdx.utils.Array;
import com.github.br.syberian.jam26.ecs.systems.cutscene.CutsceneStep;

public class CutsceneComponent extends Component {
    // Очередь шагов режиссера
    public final Array<CutsceneStep> steps = new Array<>();
    public int currentStepIndex = 0;
    public boolean isInitialized = false;
}
