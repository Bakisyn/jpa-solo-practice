package dev.milan.jpasolopractice.model;

public enum YogaRooms {
    AIR_ROOM(30),
    WATER_ROOM(25),
    EARTH_ROOM(40),
    FIRE_ROOM(20);

    private final int maxCapacity;

    YogaRooms(int maxCapacity){
        this.maxCapacity = maxCapacity;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    @Override
    public String toString() {
        return "YogaRooms{" +
                "maxCapacity=" + maxCapacity +
                '}';
    }
}
