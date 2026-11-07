package com.github.br.syberian.jam26.ecs.components;

import com.artemis.Component;

public class TriggerTargetComponent extends Component {
    public String targetTag;      // тег сущности в TagManager, на которую влияем (например, "SECURITY_CAM_1")
    public String requiredItemId; // какой предмет нужен в инвентаре ("WIRE_CUTTERS")
    public boolean isConsumeItem;   // забирать ли предмет из инвентаря после использования
}
