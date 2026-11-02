package com.github.br.libgdx.common.structure;

import com.badlogic.gdx.math.Vector2;

public class TempDataSingleton {

    public static final TempDataSingleton INSTANCE = new TempDataSingleton();

    public Vector2 tempVector = new Vector2();

}
