package Task;

import Paint.ScriptPaint;
import org.osbot.rs07.Bot;
import org.osbot.rs07.input.mouse.MouseDestination;
import org.osbot.rs07.utility.Condition;
import org.osbot.rs07.utility.ConditionalSleep;

import static Util.ScriptConstants.randomSessionGaussian;

public class Idle extends Task {

    private final ConditionalSleep sleepUntilIdle = new ConditionalSleep(60000) {
        @Override
        public boolean condition() {
            return myPlayer().getAnimation() == IDLE_ANIM_ID;
        }
    };

    public Idle(Bot bot) {
        super(bot);
    }

    @Override
    boolean shouldRun() {
        return myPlayer().getAnimation() != IDLE_ANIM_ID;
    }

    @Override
    public void runTask() throws InterruptedException {
        mouse.moveOutsideScreen();
        ScriptPaint.setStatus("Chopping (Idle)");
        log("Idling...");
        sleepUntilIdle.sleep();
        if (myPlayer().getAnimation() == -1) {
            ScriptPaint.setStatus("Simulating AFK");
            long idleTime = randomSessionGaussian();
            log(String.format("Simulating AFK for %dms", idleTime));
            sleep(idleTime);
        }
    }


}

