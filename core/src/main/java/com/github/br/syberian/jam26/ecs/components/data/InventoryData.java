package com.github.br.syberian.jam26.ecs.components.data;

import com.badlogic.gdx.utils.Array;

public class InventoryData {
    public final Array<String> items = new Array<>();
    public int maxSlots = 5;
    public boolean isDirty = true;

    public boolean addItem(String itemId) {
        if (items.size >= maxSlots) return false;
        items.add(itemId);
        isDirty = true;
        return true;
    }

    public boolean removeItem(String itemId) {
        boolean removed = items.removeValue(itemId, false);
        if (removed) isDirty = true;
        return removed;
    }

    public boolean hasItem(String itemId) {
        return items.contains(itemId, false);
    }
}
