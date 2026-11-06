package com.sugarplumstudios.coquette.item;

import net.minecraft.world.item.Item;

/**
 * A cup of strawberry tea poured from a tea set. Sip it for a little regeneration; you keep the bottle.
 * Since 1.21.2 the drinking, the effect and the bottle all come from the item's consumable and
 * use-remainder components (see ModItems), so there is nothing left to override.
 */
public class TeaCupItem extends Item {
    public TeaCupItem(Properties properties) {
        super(properties);
    }
}
