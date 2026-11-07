package com.github.br.syberian.jam26.ecs.components;

import com.artemis.Component;

public class ItemComponent extends Component {
    public String itemId; // например, "WIRE_CUTTERS", "KEY_BLUE", "MEDKIT"
    public int amount = 1;
}
