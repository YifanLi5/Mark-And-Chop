package Paint;

import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.util.ExperienceTracker;
import org.osbot.rs07.canvas.paint.Painter;
import org.osbot.rs07.input.mouse.BotMouseListener;
import org.osbot.rs07.script.Script;

import java.awt.*;
import java.awt.event.MouseEvent;

public class ScriptPaint extends BotMouseListener implements Painter {
    private final Script script;
    private final long startTime;
    private final ExperienceTracker tracker;
    private final int startingWCLvl;

    private final String[][] xpTrackTemplate = {
            {"", "+XP (XP/H)", "LVL (+)"},
            {"Woodcutting", "[1][1]", "[1][2]"},
            {"[2][0]"},
            {"[3][0]"}
    };
    private static String status;
    private final int cellWidth = 125;
    private final int cellHeight = 35;
    private static final Color GRID_BG = new Color(70, 61, 50, 156);
    private static final Color RED = new Color(255, 61, 50, 156);
    private static final Color GREEN = new Color(70, 255, 50, 156);
    private final Rectangle togglePaintRectangle;
    private Rectangle gridCanvas;
    private final Font font = new Font("Arial", Font.PLAIN, 14);
    private boolean showPaint = true;

    public ScriptPaint(Script script) {
        this.script = script;

        int maxNumCols = 0;
        for (String[] row : xpTrackTemplate) {
            maxNumCols = Math.max(maxNumCols, row.length);
        }

        this.togglePaintRectangle = new Rectangle(0, 0, cellWidth, cellHeight);
        status = "null";
        script.getBot().addPainter(this);
        script.getBot().addMouseListener(this);

        startingWCLvl = script.skills.getStatic(Skill.WOODCUTTING);

        startTime = System.currentTimeMillis();
        tracker = script.getExperienceTracker();
        tracker.start(Skill.WOODCUTTING);
    }

    @Override
    public void onPaint(Graphics2D g2d) {
        g2d.setFont(font);
        drawMouse(g2d);
        if(showPaint) {
            populatePlaceholderArray();
            drawGrid(g2d, xpTrackTemplate, cellWidth, cellHeight);
            blockAccountInfo(g2d);
        }
        drawCenteredStr(g2d, togglePaintRectangle, showPaint ? "--Hide--" : "--Show--");

    }

    public static void setStatus(String status) {
        ScriptPaint.status = status;
    }

    private void populatePlaceholderArray() {
        xpTrackTemplate[1][1] = String.format("+%s (%s)", formatNumber(tracker.getGainedXP(Skill.WOODCUTTING)), formatNumber(tracker.getGainedXPPerHour(Skill.WOODCUTTING)));
        xpTrackTemplate[1][2] = String.format("%s (+%s)", startingWCLvl, tracker.getGainedLevels(Skill.WOODCUTTING));

        xpTrackTemplate[2][0] = String.format("Status: %s", status);
        xpTrackTemplate[3][0] = formatTime(System.currentTimeMillis() - startTime);
    }


    private void drawGrid(Graphics2D g, String[][] data, int cellWidth, int cellHeight) {
        g.setFont(font);
        g.setColor(GRID_BG); //Background Color of Grid
        int maxNumCols = 0;
        for (String[] row : data) {
            maxNumCols = Math.max(maxNumCols, row.length);
        }
        if(gridCanvas == null)
            gridCanvas = new Rectangle(cellWidth * maxNumCols, cellHeight * data.length);
        g.fill(gridCanvas);
        g.setColor(Color.WHITE); // Color of Text and Grid lines
        g.draw(gridCanvas);


        // draw the horizontal lines
        for (int i = 0; i <=  data.length; i++) {
            int y = i * cellHeight;
            g.drawLine(0, y, cellWidth * maxNumCols, y);
        }

        for(int row = 0; row < data.length; row++) {
            int numElementsInRow = data[row].length;

            for(int col = 0; col < numElementsInRow; col++) {
                // draw the strings in the right positions
                int textX = col * (gridCanvas.width / numElementsInRow) + (gridCanvas.width / (numElementsInRow*2) - g.getFontMetrics().stringWidth(data[row][col]) / 2);
                int textY = row * (gridCanvas.height / data.length) + (gridCanvas.height / (data.length*2)) - g.getFontMetrics(font).getHeight() / 2 + g.getFontMetrics().getAscent();
                g.drawString(data[row][col], textX, textY);

                // draw the vertical lines. Dividing each row into {numElementsInRow} sections
                for(int i = 0; i < numElementsInRow - 1; i++) {
                    int x = col * (gridCanvas.width / numElementsInRow);
                    g.drawLine(x, row * cellHeight, x, row * cellHeight + cellHeight);
                }
            }
        }
    }


    private void drawCenteredStr(Graphics2D g2d, Rectangle rectangle, String str) {
        if(showPaint) {
            g2d.setColor(RED);
        } else {
            g2d.setColor(GREEN);
        }

        FontMetrics metrics = g2d.getFontMetrics();

        int centerX = rectangle.x + rectangle.width / 2;
        int centerY = rectangle.y + rectangle.height / 2;

        int textX = centerX - metrics.stringWidth(str) / 2;
        int textY = centerY + metrics.getAscent() / 2;

        g2d.fill(rectangle);

        g2d.setColor(Color.WHITE);
        g2d.drawString(str, textX, textY);
        g2d.draw(rectangle);
    }

    private void blockAccountInfo(Graphics2D g) {
        // blocks chat window, total XP (if toggled visible), HP, Prayer
        final int[][] widgetRootAndChild = new int[][] {
                {162, 35},
                {122, 10},
                {160, 9},
                {160, 20},
        };
        final RS2Widget[] accountInfoWidgets = new RS2Widget[widgetRootAndChild.length];

        for(int idx = 0; idx < widgetRootAndChild.length; idx++) {
            accountInfoWidgets[idx] = script.widgets.get(widgetRootAndChild[idx][0], widgetRootAndChild[idx][1]);
        }

        g.setColor(Color.BLACK);
        for (RS2Widget widget: accountInfoWidgets) {
            if(widget == null) {
                continue;
            }
            g.fill(widget.getBounds());
        }
    }

    private String formatTime(final long ms) {
        long s = ms / 1000, m = s / 60, h = m / 60;
        s %= 60;
        m %= 60;
        h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    private String formatNumber(int number) {
        if (number < 1000) {
            return String.valueOf(number);
        }
        int numKs = number / 1000;
        int hundreds = (number - numKs * 1000) / 100;
        return String.format("%d.%dk", numKs, hundreds);
    }

    @Override
    public void checkMouseEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getID() == MouseEvent.MOUSE_PRESSED) {
            Point clickPt = mouseEvent.getPoint();
            if (togglePaintRectangle.contains(clickPt)) {
                showPaint = !showPaint;
                mouseEvent.consume();
                script.log("showPaint: " + showPaint);
            }
        }
    }

    private void drawMouse(Graphics2D g) {
        Point mP = script.getMouse().getPosition();
        g.drawLine(mP.x - 5, mP.y + 5, mP.x + 5, mP.y - 5);
        g.drawLine(mP.x + 5, mP.y + 5, mP.x - 5, mP.y - 5);
    }
}
