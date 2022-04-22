package dev.milan.jpasolopractice.roomtype;

public enum RoomType {
    AIR_ROOM(30),
    WATER_ROOM(25),
    EARTH_ROOM(40),
    FIRE_ROOM(20);

    private final int maxCapacity;

    RoomType(int maxCapacity){
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
