package com.github.br.syberian.jam26.ecs.components;

import com.artemis.Component;

public class TransformComponent extends Component {
    public float x;
    public float y;
    public float rotation;

    @Override
    public String toString() {
        return "TransformComponent{" +
            "x=" + x +
            ", y=" + y +
            ", rotation=" + rotation +
            '}';
    }
}
