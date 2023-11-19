package Task;

import Paint.ScriptPaint;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.event.EventQueue;
import org.osbot.rs07.input.mouse.ClickMouseEvent;
import org.osbot.rs07.input.mouse.InventorySlotDestination;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import static Util.ScriptConstants.SESSION_DROP_SKIP;
import static Util.ScriptConstants.logsFilter;
import static java.awt.event.KeyEvent.VK_SHIFT;

@SuppressWarnings("rawtypes")
public class Drop extends Task {
    /**
     * Inventory slots visual guide
     * 0  1  2  3
     * 4  5  6  7
     * 8  9 10 11
     * 12 13 14 15
     * 16 17 18 19
     * 20 21 22 23
     * 24 25 26 27
     */
    private final static int[][] DROP_ORDERS = {
            {0, 1, 2, 3, 7, 6, 5, 4, 8, 9, 10, 11, 15, 14, 13, 12, 16, 17, 18, 19, 23, 22, 21, 20, 24, 25, 26, 27},
            {3, 2, 1, 0, 4, 5, 6, 7, 11, 10, 9, 8, 12, 13, 14, 15, 19, 18, 17, 16, 20, 21, 22, 23, 27, 26, 25, 24},
            {0, 4, 8, 12, 16, 20, 24, 25, 21, 17, 13, 9, 5, 1, 2, 6, 10, 14, 18, 22, 26, 27, 23, 19, 15, 11, 7, 3},
            {3, 7, 11, 15, 19, 23, 27, 26, 22, 18, 14, 10, 6, 2, 1, 5, 9, 13, 17, 21, 25, 24, 20, 16, 12, 8, 4, 0},
            {3, 7, 2, 6, 1, 5, 0, 4, 8, 12, 9, 13, 10, 14, 11, 15, 19, 23, 18, 22, 17, 21, 16, 20, 27, 26, 25, 24},
            {27, 26, 25, 24, 20, 21, 22, 23, 19, 18, 17, 16, 12, 13, 14, 15, 11, 10, 9, 8, 4, 5, 6, 7, 3, 2, 1, 0}
    };
    private final static HashMap<Integer, InventorySlotDestination> INVENTORY_SLOT_DESTINATION_MAPPING = new HashMap<>();



    public Drop(Bot bot) {
        super(bot);
        mapInvSlotDestinations();
    }

    @Override
    int probabilityWeight() {
        return 4;
    }

    @Override
    boolean shouldRun() {
        boolean doPrematureDrop = myPlayer().getAnimation() == IDLE_ID && inventory.getEmptySlots() < random(4, 12);
        return (inventory.isFull() || doPrematureDrop) && inventory.contains(logsFilter);
    }

    @Override
    public void runTask() throws InterruptedException {
        customDropAll();
    }

    private boolean customDropAll() throws InterruptedException {
        ScriptPaint.setStatus("Dropping logs");
        if (!inventory.getSlotBoundingBox(0).equals(INVENTORY_SLOT_DESTINATION_MAPPING.get(0).getBoundingBox())) {
            warn("inventory slots bounding boxes no longer correct. Attempting to remap inventory slots.");
            mapInvSlotDestinations();
        }
        if (!inventory.getSlotBoundingBox(0).equals(INVENTORY_SLOT_DESTINATION_MAPPING.get(0).getBoundingBox())) {
            warn("inventory slots bounding boxes no longer correct even after remap operation. " +
                    "Was the client window resized?");
            bot.getScriptExecutor().stop(false);
        }

        EventQueue mouseShiftDropClicks = new EventQueue();
        for (int invSlot : DROP_ORDERS[random(0, DROP_ORDERS.length - 1)]) {
            Item itemAtInvSlot = inventory.getItemInSlot(invSlot);
            if (itemAtInvSlot == null || itemAtInvSlot.nameContains("Bird nest") || random(1000) <= SESSION_DROP_SKIP) {
                continue;
            } else if (itemAtInvSlot.getName().endsWith("logs") || itemAtInvSlot.getName().equals("Logs")) {
                mouseShiftDropClicks.add(new ClickMouseEvent(INVENTORY_SLOT_DESTINATION_MAPPING.get(invSlot)));
            }
        }

        if (inventory.isItemSelected()) {
            inventory.deselectItem();
        }

        keyboard.pressKey(VK_SHIFT);
        execute(mouseShiftDropClicks);
        keyboard.releaseKey(VK_SHIFT);
        sleep(random(500, 1000));
        if (inventory.contains(logsFilter)) {
            inventory.dropAll(logsFilter);

        }
        return !inventory.contains(logsFilter);
    }

    private void mapInvSlotDestinations() {
        for (int i = 0; i < 28; i++) {
            INVENTORY_SLOT_DESTINATION_MAPPING.put(i, new InventorySlotDestination(bot, i));
        }
    }
}
