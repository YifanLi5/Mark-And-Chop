package Task;

import Paint.ScriptPaint;
import Util.ScriptConstants;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.utility.ConditionalLoop;

import java.util.List;

public class GetBirdNest extends Task implements MessageListener {
    class PickUpLoop extends ConditionalLoop {
        public PickUpLoop(Bot bot, int i) {
            super(bot, i);
        }

        @Override
        public boolean condition() {
            List<GroundItem> nests = groundItems.filter(groundItem -> groundItem.getName().contains("Bird nest") && groundItem.getId() != 12345);
            if(nests == null || nests.isEmpty()) {
                warn("GetBirdNest executed due to game message but script unable to find a bird nest.");
                return true;
            }
            return !nests.get(0).interact("Pick up");

        }
    }

    private boolean birdNestDetected;

    public GetBirdNest(Bot bot) {
        super(bot);
        bot.addMessageListener(this);
        this.birdNestDetected = false;
    }


    @Override
    boolean shouldRun() {
        return this.birdNestDetected;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void runTask() throws InterruptedException {
        ScriptPaint.setStatus("AFK before collecting nest");
        sleep(ScriptConstants.randomSessionGaussian());
        ScriptPaint.setStatus("Collecting bird nest");

        List<GroundItem> nests = groundItems.filter(groundItem ->
                (groundItem.getName().contains("Bird nest") || groundItem.getName().contains("Clue nest"))
                        && groundItem.getId() != 5075);
        if(nests == null || nests.isEmpty()) {
            warn("GetBirdNest executed due to game message but script unable to find a bird nest.");
            return;
        }

        ConditionalLoop pickUpLoop = new PickUpLoop(this.bot, 5);
        pickUpLoop.start();

        if(!pickUpLoop.getResult()) {
            warn("Script was unable to pick up bird nest.");
        }
    }


    @Override
    public void onMessage(Message message) {
        if(message.getMessage().contains("A bird's nest falls out") && message.getType().equals(Message.MessageType.GAME)) {
            this.birdNestDetected = true;
        }
    }
}
