package Task;

import Paint.ScriptPaint;
import org.osbot.rs07.Bot;
import org.osbot.rs07.utility.ConditionalSleep;

import static Util.ScriptConstants.randomSessionGaussian;

public class Idle extends Task {

    private int conditionCheckCount = 0;
    private int conditionCheckThreshold = random(0, 8);
    private final ConditionalSleep sleepUntilIdle = new ConditionalSleep(60000, 1000, 500) {

        @Override
        public boolean condition() {
            // hacky way of moving mouse offscreen after a random amount of time but only if the player is not idle
            // remember, condition returning true exits ConditionalSleep
            conditionCheckCount += 1;
            if (conditionCheckCount > conditionCheckThreshold && mouse.isOnScreen()) {
                mouse.moveOutsideScreen();
            }
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
        conditionCheckCount = 0;
        conditionCheckThreshold = random(3, 8);

        ScriptPaint.setStatus("Chopping (Idle)");
        camera.setMiddleMouseControl(true);
        boolean sleepInterrupted;
        int antiLogoutCounter = 0;
        int maxSleeps = random(1,4);
        do {
            sleepInterrupted = sleepUntilIdle.sleep();
            antiLogoutCounter += 1;
            log(String.format("antiLogoutCounter: %d / %d", antiLogoutCounter, maxSleeps));
            if(antiLogoutCounter >= maxSleeps) {
                log("anti-logout camera move");
                ScriptPaint.setStatus("Camera move: Anti-Logout");
                camera.moveYaw(camera.getYawAngle() + random(-15, 15));
                antiLogoutCounter = 0;
                maxSleeps = random(1,4);
            }
            ScriptPaint.setStatus("Chopping (Idle)");
        } while(!sleepInterrupted);

        if (myPlayer().getAnimation() == -1 && !mouse.isOnScreen()) {
            ScriptPaint.setStatus("Simulating AFK");
            long idleTime = randomSessionGaussian();
            log(String.format("Simulating AFK for %dms", idleTime));
            sleep(idleTime);
        }
    }
}

