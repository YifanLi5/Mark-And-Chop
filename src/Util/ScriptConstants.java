package Util;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.model.Item;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ScriptConstants {

    public static final String[] CHOP = {"Chop down", "Cut"};

    public static final int SESSION_MEAN;

    public static final int SESSION_STD_DEV;

    public static final int SESSION_DROP_SKIP;

    public static final Filter<Item> logsFilter = item -> item.getName().endsWith("logs") || item.getName().equals("Logs");

    static {
        ThreadLocalRandom current = ThreadLocalRandom.current();
        SESSION_MEAN = current.nextInt(7500, 10000);
        SESSION_STD_DEV = current.nextInt(1500, 3000);
        SESSION_DROP_SKIP = current.nextInt(30);
    }

    public static int randomSessionGaussian() {
        return (int) Math.abs((new Random().nextGaussian() * SESSION_STD_DEV + SESSION_MEAN));
    }
}
