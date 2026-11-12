package com.github.br.syberian.jam26.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.github.br.syberian.jam26.ActorTypeEnum;
import com.github.br.syberian.jam26.IUserActorFactory;
import com.github.br.syberian.jam26.ecs.components.HudComponent;
import com.github.br.syberian.jam26.ecs.components.PlayerComponent;
import com.github.br.syberian.jam26.ecs.components.RenderComponent;
import com.github.br.syberian.jam26.ecs.components.data.InventoryData;

@All({HudComponent.class, RenderComponent.class})
public class HudInventorySystem extends IteratingSystem {

    private ComponentMapper<RenderComponent> mRender;
    private ComponentMapper<PlayerComponent> mPlayer;
    private EntitySubscription playerSubscription;

    private IUserActorFactory userActorFactory;

    public void setUserActorFactory(IUserActorFactory userActorFactory) {
        this.userActorFactory = userActorFactory;
    }

    @Override
    protected void initialize() {
        mRender = world.getMapper(RenderComponent.class);
        mPlayer = world.getMapper(PlayerComponent.class);

        playerSubscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(PlayerComponent.class));
    }

    @Override
    protected void process(int hudEntityId) {
        IntBag players = playerSubscription.getEntities();
        if (players.isEmpty()) return;

        PlayerComponent player = mPlayer.get(players.get(0));
        InventoryData inventory = player.inventoryData;

        if (!inventory.isDirty) return;

        RenderComponent render = mRender.get(hudEntityId);
        if (render.actor instanceof Table) {
            Table rootTable = (Table) render.actor;
            Table slotsTable = rootTable.findActor("slots_table");
            if (slotsTable != null) {
                rebuildInventoryView(slotsTable, inventory);
                inventory.isDirty = false;
            }
        }
    }

    private void rebuildInventoryView(Table table, InventoryData inventory) {
        table.clearChildren();

        for (int i = 0; i < inventory.items.size; i++) {
            String itemId = inventory.items.get(i);
            ActorTypeEnum iconType = ActorTypeEnum.valueOf(itemId);
            Actor icon = userActorFactory.getCustomUiActor(iconType);
            table.add(icon).size(64f, 64f).padRight(16f);
        }
    }

}
