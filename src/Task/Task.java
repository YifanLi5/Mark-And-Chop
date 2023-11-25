package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.script.MethodProvider;

import java.util.ArrayList;

public abstract class Task extends MethodProvider {
    static final int IDLE_ANIM_ID = -1;
    static ArrayList<Task> subclassInstances = new ArrayList<>();

    public Task(Bot bot) {
        exchangeContext(bot);
        subclassInstances.add(this);

        log("Initialized task class: " + this.getClass().getCanonicalName());
    }

    public static Task pollNextTask() {
        int weightingSum = 0;
        ArrayList<Task> runnableTasks = new ArrayList<>();
        for (Task task : Task.subclassInstances) {
            if (task.shouldRun()) {
                runnableTasks.add(task);
                weightingSum += task.probabilityWeight();
            }
        }
        if (runnableTasks.isEmpty()) {
            return null;
        } else if (runnableTasks.size() == 1) {
            return runnableTasks.get(0);
        }

        int roll = random(weightingSum);
        int idx = 0;
        for (; idx < runnableTasks.size(); idx++) {
            roll -= runnableTasks.get(idx).probabilityWeight();
            if (roll < 0) {
                break;
            }
        }

        return runnableTasks.get(idx);
    }

    abstract boolean shouldRun();

    public abstract void runTask() throws InterruptedException;

    int probabilityWeight() {
        return 1;
    }
}
