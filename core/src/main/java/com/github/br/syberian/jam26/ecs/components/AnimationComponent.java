package com.github.br.syberian.jam26.ecs.components;

import com.artemis.Component;
import com.artemis.PooledComponent;
import com.github.ashvard.gdx.simple.animation.component.SimpleAnimationComponent;

public class AnimationComponent extends PooledComponent {

    public transient SimpleAnimationComponent simpleAnimationComponent;

    public AnimationComponent(SimpleAnimationComponent simpleAnimationComponent) {
        this.simpleAnimationComponent = simpleAnimationComponent;
    }

    public AnimationComponent(){}

    @Override
    protected void reset() {
        simpleAnimationComponent = null;
    }

}
