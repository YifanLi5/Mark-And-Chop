
import Paint.ScriptPaint;
import Paint.TreeSelectionPainter;
import Task.*;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;


import java.util.HashMap;
import java.util.Map;

@ScriptManifest(author = "yfoo", name = "Mark N' Chop", info = "Mark trees with paint, then chop", version = 0.1, logo = "")
public class MainScript extends Script {

    @Override
    public void onStart() throws InterruptedException {
        TreeSelectionPainter treeSelectionPainter = new TreeSelectionPainter(this);

        boolean selectionComplete = treeSelectionPainter.isSelectionComplete();
        while(!selectionComplete) {
            sleep(1000);
            selectionComplete = treeSelectionPainter.isSelectionComplete();
        }

        HashMap<Position, String> selectedTrees = treeSelectionPainter.getSelectedTreesAndCleanupPainter();
        StringBuffer msgBuffer = new StringBuffer();
        msgBuffer.append("Using Trees:\n");
        for (Map.Entry<Position, String> tree: selectedTrees.entrySet()) {
            msgBuffer.append(String.format("%s @ %s\n", tree.getValue(), tree.getKey()));
        }
        log(msgBuffer);
        new ScriptPaint(this);

        new Chop(this.bot, selectedTrees);
        new Drop(this.bot);
        new Idle(this.bot);

    }

    @Override
    public int onLoop() throws InterruptedException {
        Task currentTask = Task.pollNextTask();
        if (currentTask != null) {
            currentTask.runTask();
        }
        return random(1000);
    }
}
