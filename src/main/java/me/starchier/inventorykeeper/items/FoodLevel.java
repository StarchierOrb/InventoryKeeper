package me.starchier.inventorykeeper.items;

import me.starchier.inventorykeeper.util.Debugger;

import java.util.Random;

public class FoodLevel {
    public static final int KEEP_HUNGER = 1;
    public static final int RESET_HUNGER = 2;
    public static final int FIXED_HUNGER = 3;
    private static final Random random = new Random();

    private final String foodLevel;
    private final String saturationLevel;
    private final int restoreFoodType;
    private final int restoreSaturationType;

    public FoodLevel(String foodLevel, String saturationLevel) {
        if (foodLevel.startsWith("keep")) {
            restoreFoodType = KEEP_HUNGER;
        } else if (foodLevel.startsWith("reset")) {
            restoreFoodType = RESET_HUNGER;
        } else {
            restoreFoodType = FIXED_HUNGER;
        }
        this.foodLevel = foodLevel.split(",", 2)[1];
        if (saturationLevel.startsWith("keep")) {
            restoreSaturationType = KEEP_HUNGER;
        } else if (saturationLevel.startsWith("reset")) {
            restoreSaturationType = RESET_HUNGER;
        } else {
            restoreSaturationType = FIXED_HUNGER;
        }
        this.saturationLevel = saturationLevel.split(",", 2)[1];
    }

    public int getRestoreFoodType() {
        return restoreFoodType;
    }

    public int getRestoreSaturationType() {
        return restoreSaturationType;
    }

    public int getFinalFoodLevel(int originFood) {
        if (restoreFoodType == KEEP_HUNGER) {
            return originFood;
        } else if (restoreFoodType == RESET_HUNGER) {
            return 20;
        } else if (restoreFoodType == FIXED_HUNGER) {
            return processNumber(foodLevel, 20);
        }
        return 20;
    }

    public int getFinalSaturationLevel(int originSaturation) {
        if (restoreSaturationType == KEEP_HUNGER) {
            return originSaturation;
        } else if (restoreSaturationType == RESET_HUNGER) {
            return 5;
        } else if (restoreSaturationType == FIXED_HUNGER) {
            return processNumber(saturationLevel, 5);
        }
        return 5;
    }

    private static int processNumber(String src, int def) {
        try {
            if (src.contains(",")) {
                String[] temp = src.split(",");
                int[] levels = {Integer.parseInt(temp[0]), Integer.parseInt(temp[1])};
                return random.nextInt(levels[0] + levels[1] + 1) - Math.min(levels[0], levels[1]) - 1;
            } else {
                return Integer.parseInt(src);
            }
        } catch (Exception e) {
            Debugger.logDebugMessage("Error creating a random number: " + e.getMessage());
        }
        return def;
    }
}
