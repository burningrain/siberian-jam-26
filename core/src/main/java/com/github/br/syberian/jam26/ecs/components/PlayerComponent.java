package com.github.br.syberian.jam26.ecs.components;

import com.artemis.Component;
import com.github.br.syberian.jam26.data.InventoryData;

public class PlayerComponent extends Component {

    public InventoryData inventoryData = new InventoryData();
    public PlayerComponent() {
    }

}
