package com.example.expertschedule.planner.domain;

public enum OrderPriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int level;

    OrderPriority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
