package Task;


import Paint.ScriptPaint;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.input.mouse.MouseDestination;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.utility.Condition;
import org.osbot.rs07.utility.ConditionalLoop;

import java.util.List;

public class GetBirdNest extends Task implements MessageListener {
    private boolean birdNestDetected;

    public GetBirdNest(Bot bot) {
        super(bot);
        bot.addMessageListener(this);
        this.birdNestDetected = false;
    }

    @Override
    int probabilityWeight() {
        return 99;
    }

    @Override
    boolean shouldRun() {
        return this.birdNestDetected;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void runTask() throws InterruptedException {
        ScriptPaint.setStatus("Picking up bird nest");
        List<GroundItem> nests = groundItems.filter(groundItem ->
                (groundItem.getName().contains("Bird nest") || groundItem.getName().contains("Clue nest"))
                        && groundItem.getId() != 5075);
        if (nests == null || nests.isEmpty()) {
            warn("GetBirdNest executed due to game message but script unable to find a bird nest.");
            return;
        }

        ConditionalLoop pickUpLoop = new DoWhilePickUpNest(this.bot, 5);
        pickUpLoop.start();

        if (!pickUpLoop.getResult()) {
            warn("Script was unable to pick up bird nest.");
        }
        this.birdNestDetected = false;
        sleep(random(500, 1000));
        shiftNestsUp();
    }

    private void shiftNestsUp() {

        int logsIdx = inventory.getSlot(item -> item.getName().endsWith("logs") || item.getName().equals("Logs"));
        int nestIdx = inventory.getSlot(item -> item.getName().contains("Bird nest") || item.getName().contains("Clue nest"));
        if (logsIdx != -1 && nestIdx != -1 && nestIdx > logsIdx) {
            log("DO NOT RESIZE SCREEN DURING INVENTORY SHIFT OPERATION!!!");
            MouseDestination a = inventory.getMouseDestination(logsIdx);
            MouseDestination b = inventory.getMouseDestination(nestIdx);
            mouse.continualClick(a, new Condition() {
                @Override
                public boolean evaluate() {
                    return mouse.move(b, true);
                }
            });
            log("Ok to resize.");
        }
    }

    @Override
    public void onMessage(Message message) {
        if (message.getMessage().contains("A bird's nest falls out") && message.getType().equals(Message.MessageType.GAME)) {
            log("Detected bird nest message.");
            this.birdNestDetected = true;
        }
    }

    @SuppressWarnings("unchecked")
    class DoWhilePickUpNest extends ConditionalLoop {
        public DoWhilePickUpNest(Bot bot, int i) {
            super(bot, i);
        }

        @Override
        public boolean condition() {
            // 5075 is trade-able bird nest, prevent lures
            List<GroundItem> nests = groundItems.filter(groundItem ->
                    (groundItem.getName().contains("Bird nest") || groundItem.getName().contains("Clue nest"))
                            && groundItem.getId() != 5075);
            if (nests == null || nests.isEmpty()) {
                warn("GetBirdNest executed due to game message but script unable to find a bird nest.");
                return true;
            }
            return !nests.get(0).interact("Take");

        }
    }
}
