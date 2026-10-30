package com.github.br.libgdx.common.structure.screen;

public class EqualsUtils {

    private EqualsUtils(){}

    public static boolean isEquals(float f1, float f2, float inaccuracy) {
        return Math.abs(f1 - f2) < inaccuracy;
    }

}
