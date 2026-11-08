package com.github.br.syberian.jam26.ecs.components;

import com.artemis.Component;

public class TransformComponent extends Component {
    // в пикселях
    public float x;
    public float y;
    public float rotationDegree; // в градусах

    @Override
    public String toString() {
        return "TransformComponent{" +
            "x=" + x +
            ", y=" + y +
            ", rotation=" + rotationDegree +
            '}';
    }
}
