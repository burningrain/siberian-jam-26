package com.github.ashvard.gdx.simple.animation.io.interpret.expression;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.ashvard.gdx.simple.animation.fsm.FsmContext;

public class BooleanExpression implements Expression {

    private final boolean exp;
    private final String variable;

    public BooleanExpression(boolean exp, String variable) {
        this.exp = exp;
        this.variable = variable;
    }

    @Override
    public boolean eq(FsmContext context) {
        Boolean bool = context.get(variable);
        if (bool == null) {
            throw new GdxRuntimeException("variable [" + variable + "] is null");
        }
        return (boolean) bool  == exp;
    }

    @Override
    public boolean gt(FsmContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean gtOrEq(FsmContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean lt(FsmContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean ltOrEq(FsmContext context) {
        throw new UnsupportedOperationException();
    }

}
