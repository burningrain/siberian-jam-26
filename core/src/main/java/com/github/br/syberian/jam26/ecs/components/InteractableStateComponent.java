package com.github.br.syberian.jam26.ecs.components;

import com.artemis.Component;

public class InteractableStateComponent extends Component {
    public boolean isOpen = false;
    public boolean isLocked = false;
    public String requiredItemIdFromPayerInventory; // например, "KEY_1" или null, если не заперто
    public int openFrameNumber = 1;
    public int closedFrameNumber = 0;
}
