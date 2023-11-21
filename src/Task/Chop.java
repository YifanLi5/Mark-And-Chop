package Task;

import Paint.ScriptPaint;
import Util.UserSelectedTreesFilter;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.event.Event;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.utility.ConditionalLoop;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static Util.ScriptConstants.*;

public class Chop extends Task {
    class FindTreeAndChopLoop extends ConditionalLoop {

        private final UserSelectedTreesFilter userSelectedTreesFilter;
        public FindTreeAndChopLoop(Bot bot, int i, HashMap<Position, String> selectedTrees) {
            super(bot, i);
            this.userSelectedTreesFilter = new UserSelectedTreesFilter(selectedTrees);
        }

        @Override
        public boolean condition() {
            RS2Object nextTree;
            List<RS2Object> validTrees = objects.filter(userSelectedTreesFilter).stream().distinct().collect(Collectors.toList());
            if (validTrees.isEmpty()) {
                warn("userSelectedTreesFilter returned nothing.");
                return false;
            }

            log(String.format("Found %s valid trees", validTrees.size()));
            nextTree = validTrees.get(random(validTrees.size()));
            if(nextTree == null) {
                warn("nextTree is null");
                return false;
            }

            Event chopEvent = new InteractionEvent(nextTree, CHOP)
                    .setOperateCamera(false)
                    .setBlocking();
            ScriptPaint.setStatus("Attempting to chop...");
            execute(chopEvent);
            if(chopEvent.hasFailed()) {
                warn("chop event failed. Retrying...");
            }

            boolean result = new ConditionalSleep(5000) {
                @Override
                public boolean condition() {
                    return myPlayer().getAnimation() != IDLE_ANIM_ID;
                }
            }.sleep();
            return !result;
        }
    }

    private final Filter<Item> woodcuttingAxeFilter = item -> {
        // Ends with " axe" or " axe (or)"
        return item.getName().matches(".* axe(\\s\\(or\\))?$") && !item.getName().equals("Blessed axe");
    };

    private final HashMap<Position, String> selectedTrees;

    public Chop(Bot bot, HashMap<Position, String> selectedTrees) {
        super(bot);
        this.selectedTrees = selectedTrees;
    }

    @Override
    int probabilityWeight() {
        return 2;
    }

    @Override
    boolean shouldRun() {
        return myPlayer().getAnimation() == Task.IDLE_ANIM_ID && !inventory.isFull();
    }

    @Override
    public void runTask() throws InterruptedException {

        if (!(inventory.contains(woodcuttingAxeFilter)) && !(equipment.isWieldingWeapon(woodcuttingAxeFilter))) {
            warn("Inventory/Equipment does not contain a suitable woodcutting axe");
            bot.getScriptExecutor().stop(false);
        }


        if (inventory.isItemSelected()) {
            inventory.deselectItem();
        }

        if (equipment.isWieldingWeapon("Dragon axe") && combat.getSpecialPercentage() == 100) {
            ScriptPaint.setStatus("D-axe special");
            combat.toggleSpecialAttack(true);
        }

        ConditionalLoop loop = new FindTreeAndChopLoop(bot, 5, selectedTrees);
        loop.start();
        if(!loop.getResult()) {
            warn("Unable to find selected trees. Exiting...");
            bot.getScriptExecutor().stop(false);
        }
        ScriptPaint.setStatus("Chopping...");
    }
}


