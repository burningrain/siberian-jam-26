package com.github.ashvard.gdx.simple.animation.io.interpret.expression;

import com.github.ashvard.gdx.simple.animation.fsm.FsmContext;

public interface Expression {

    boolean gt(FsmContext context);

    boolean gtOrEq(FsmContext context);

    boolean lt(FsmContext context);

    boolean ltOrEq(FsmContext context);

    boolean eq(FsmContext context);

}
