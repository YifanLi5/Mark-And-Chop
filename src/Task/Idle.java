package Task;

import Paint.ScriptPaint;
import org.osbot.rs07.Bot;
import org.osbot.rs07.input.mouse.MouseDestination;
import org.osbot.rs07.utility.Condition;
import org.osbot.rs07.utility.ConditionalSleep;

import static Util.ScriptConstants.CHOP_ANIM_ID;
import static Util.ScriptConstants.randomSessionGaussian;

public class Idle extends Task {

    private final ConditionalSleep sleepWhileChopping = new ConditionalSleep(60000) {
        @Override
        public boolean condition() {
            return !(myPlayer().getAnimation() == CHOP_ANIM_ID);
        }
    };

    public Idle(Bot bot) {
        super(bot);
    }

    @Override
    boolean shouldRun() {
        return myPlayer().getAnimation() == CHOP_ANIM_ID;
    }

    @Override
    public void runTask() throws InterruptedException {
        shiftBottlesUp();
        ScriptPaint.setStatus("Chopping (Idle)");
        log("Idling...");
        sleepWhileChopping.sleep();
        if (myPlayer().getAnimation() == -1) {
            ScriptPaint.setStatus("Simulating AFK");
            long idleTime = randomSessionGaussian();
            log(String.format("Simulating AFK for %dms", idleTime));
            sleep(idleTime);
        }
    }

    // Swap clue bottles to first slot occupied by a fish
    private void shiftBottlesUp() throws InterruptedException {
        int logsIdx = inventory.getSlot(item -> item.getName().endsWith("logs") || item.getName().equals("Logs"));
        int nestIdx = inventory.getSlotForNameThatContains("Bird nest");
        if (logsIdx != -1 && nestIdx != -1 && nestIdx > logsIdx) {
            log("DO NOT RESIZE SCREEN DURING INVENTORY SHIFT OPERATION!!!");
            sleep(1000);
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
}

