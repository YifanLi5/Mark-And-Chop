package Paint;

import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.canvas.paint.Painter;
import org.osbot.rs07.input.mouse.BotMouseListener;
import org.osbot.rs07.script.Script;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class TreeSelectionPainter extends BotMouseListener implements Painter {

    private final Script script;
    HashMap<RS2Object, Rectangle> trees;
    HashSet<RS2Object> selectedTrees;
    private final Filter<RS2Object> filterOutAlreadySelected = new Filter<RS2Object>() {
        @Override
        public boolean match(RS2Object rs2Object) {
            boolean result = true;
            for(RS2Object alreadySelected: selectedTrees) {
                if(alreadySelected.getPosition().equals(rs2Object.getPosition())) {
                    result = false;
                    break;
                }
            }
            return result;
        }
    };

    private Rectangle finishSelectionRect;
    private boolean selectionComplete = false;

    private static final Color MY_GREEN = new Color(25, 240, 25, 156);

    private int frameCounter = 0;
    public TreeSelectionPainter(Script script) {
        this.script = script;
        this.selectedTrees = new HashSet<>();
        this.trees = new HashMap<>();
        script.getBot().addPainter(this);
        script.getBot().addMouseListener(this);

        queryTrees();
    }
    @Override
    public void onPaint(Graphics2D g2d) {
        frameCounter += 1;
        if(frameCounter % 25 == 0) {
            queryTrees();
        }

        for(RS2Object tree: trees.keySet()) {
            if(selectedTrees.contains(tree)){
                g2d.setColor(Color.GREEN);
            } else {
                g2d.setColor(Color.RED);
            }
            g2d.draw(trees.get(tree));
        }

        finishSelectionRect = drawCenteredStr(g2d, "Finish Selection");
    }

    public boolean isSelectionComplete() {
        return selectionComplete;
    }

    public HashMap<Position, String> getSelectedTreesAndCleanupPainter() {
        if(!selectionComplete) {
            return null;
        } else if(selectedTrees.isEmpty()) {
            return null;
        }
        script.getBot().removePainter(this);
        script.getBot().removeMouseListener(this);
        HashMap<Position, String> namePositionMapping = new HashMap<>();
        for(RS2Object tree: selectedTrees) {
            namePositionMapping.put(tree.getPosition(), tree.getName());
        }
        return namePositionMapping;
    }

    @SuppressWarnings("unchecked")
    private void queryTrees() {
        List<RS2Object> visibleTrees = script.objects.filter(
                new ActionFilter<>("Chop down"),
                rs2Object -> rs2Object.isVisible() &&
                        script.myPlayer().getPosition().distance(rs2Object.getPosition()) <= 10
        );

        if(visibleTrees.isEmpty()) {
            script.log("Found No trees");
            return;
        }
        trees.clear();
        for (RS2Object newlyQueriedTree: visibleTrees) {
            trees.put(newlyQueriedTree, newlyQueriedTree.getModel().getBoundingBox(newlyQueriedTree.getGridX(), newlyQueriedTree.getGridY(), newlyQueriedTree.getZ()));
        }

    }

    @Override
    public void checkMouseEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getID() == MouseEvent.MOUSE_PRESSED) {
            Point clickPt = mouseEvent.getPoint();
            for(Map.Entry<RS2Object, Rectangle> treeEntry: trees.entrySet()) {
                if(finishSelectionRect != null && finishSelectionRect.contains(clickPt)) {
                    selectionComplete = true;
                    mouseEvent.consume();
                }
                else if(treeEntry.getValue().contains(clickPt)) {
                    if (!selectedTrees.remove(treeEntry.getKey())) {
                        selectedTrees.add(treeEntry.getKey());
                    }
                    mouseEvent.consume();
                }
            }
        }
    }

    private Rectangle drawCenteredStr(Graphics2D g2d, String str) {
        g2d.setColor(MY_GREEN);

        FontMetrics metrics = g2d.getFontMetrics();

        int rectWidth = metrics.stringWidth(str) + 30;
        int rectHeight = metrics.getHeight() + 30;

        int x = 0;
        int y = 0;

        Rectangle rectangle = new Rectangle(x, y, rectWidth, rectHeight);

        g2d.fill(rectangle);

        int textX = x + 15;
        int textY = y + 15 + metrics.getAscent();

        g2d.setColor(Color.WHITE);
        g2d.drawString(str, textX, textY);

        return rectangle;
    }


    private boolean entityBoundsHasMyMouse(Entity entity) {
        List<Polygon> polygonList = script.getDisplay().getModelMeshTriangles(entity.getGridX(), entity.getGridY(),
                entity.getZ(), entity.getModel());
        if (polygonList != null && !polygonList.isEmpty()) {
            return polygonList.stream().anyMatch(polygon -> polygon.contains(script.getMouse().getPosition()));
        }
        return false;
    }
}
