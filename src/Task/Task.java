package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.script.MethodProvider;

import java.util.ArrayList;

public abstract class Task extends MethodProvider {
    static ArrayList<Task> subclassInstances = new ArrayList<>();
    static final int IDLE_ANIM_ID = -1;

    public Task(Bot bot) {
        exchangeContext(bot);
        subclassInstances.add(this);
    }

    abstract boolean shouldRun();

    public abstract void runTask() throws InterruptedException;

    int probabilityWeight() {
        return 1;
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
}
