package Paint;

import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.util.ExperienceTracker;
import org.osbot.rs07.canvas.paint.Painter;
import org.osbot.rs07.input.mouse.BotMouseListener;
import org.osbot.rs07.script.Script;

import java.awt.*;
import java.awt.event.MouseEvent;

public class ScriptPaint extends BotMouseListener implements Painter {
    private static final Color GRAY = new Color(70, 61, 50, 156);
    private boolean showPaint = true;
    private final Script script;
    private final long startTime;
    private final ExperienceTracker tracker;

    private final int wcLvlStart;

    private final String[][] xpTrackTemplate = {
        {"", "+XP (XP/H)", "LVL (+)"},
        {"Woodcutting", "", ""}
    };

    private final String[][] statusAndRuntime = {
        {null},
        {null}
    };

    private static String status;

    private final int cellWidth = 100;
    private final int cellHeight = 50;

    private final Rectangle togglePaint = new Rectangle(0, 0, cellWidth, cellHeight);
    private final Font font = new Font("Arial", Font.PLAIN, 14);

    public ScriptPaint(Script script) {
        this.script = script;
        status = "null";
        script.getBot().addPainter(this);
        script.getBot().addMouseListener(this);

        wcLvlStart = script.skills.getStatic(Skill.WOODCUTTING);

        startTime = System.currentTimeMillis();
        tracker = script.getExperienceTracker();
        tracker.start(Skill.WOODCUTTING);
    }

    @Override
    public void onPaint(Graphics2D g2d) {
        g2d.setFont(font);
        drawMouse(g2d);
        if(showPaint) {
            populateDataGrid(g2d);
            populateStatusAndRuntime(g2d);
        }
        drawCenteredStr(g2d, togglePaint, showPaint ? "--Hide--" : "--Show--");
    }

    public static void setStatus(String status) {
        ScriptPaint.status = status;
    }

    private void populateDataGrid(Graphics2D g2d) {
        xpTrackTemplate[1][1] = String.format("+%s (%s)", formatNumber(tracker.getGainedXP(Skill.WOODCUTTING)), formatNumber(tracker.getGainedXPPerHour(Skill.WOODCUTTING)));
        xpTrackTemplate[1][2] = String.format("%s (+%s)", wcLvlStart, tracker.getGainedLevels(Skill.WOODCUTTING));
        drawGrid(g2d, xpTrackTemplate, 0, 0, cellWidth, cellHeight);
    }

    private void populateStatusAndRuntime(Graphics2D g2d) {
        statusAndRuntime[0][0] = String.format("Status: %s", status);
        statusAndRuntime[1][0] = formatTime(System.currentTimeMillis() - startTime);
        drawGrid(g2d, statusAndRuntime, 0, cellHeight * xpTrackTemplate.length, cellWidth * xpTrackTemplate[0].length, 25);
    }

    private void drawGrid(
            Graphics2D g2d,
            String[][] data,
            int originX,
            int originY,
            int cellWidth,
            int cellHeight
    ) {


        int numRows = data.length;
        int numCols = data[0].length;
        int gridWidth = numCols * cellWidth;
        int gridHeight = numRows * cellHeight;

        g2d.setColor(GRAY);
        g2d.fillRect(originX, originY, gridWidth, gridHeight);

        g2d.setColor(Color.WHITE);

        for (int i = 0; i <= numRows; i++) {
            int y = originY + i * cellHeight;
            g2d.drawLine(originX, y, originX + gridWidth, y);
        }

        for (int i = 0; i <= numCols; i++) {
            int x = originX + i * cellWidth;
            g2d.drawLine(x, originY, x, originY + gridHeight);
        }

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                String cellData = data[i][j];
                int x = originX + j * cellWidth + (cellWidth - g2d.getFontMetrics().stringWidth(cellData)) / 2;
                int y = originY + i * cellHeight + (cellHeight - g2d.getFontMetrics().getHeight()) / 2
                        + g2d.getFontMetrics().getAscent();
                g2d.drawString(cellData, x, y);
            }
        }
    }

    private void drawCenteredStr(Graphics2D g2d, Rectangle rectangle, String str) {
        g2d.setColor(GRAY);
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
            if (new Rectangle(0, 0, cellWidth, cellHeight).contains(clickPt)) {
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
