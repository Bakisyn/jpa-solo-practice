package dev.milan.jpasolopractice.room.util;

import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.room.Room;
import dev.milan.jpasolopractice.roomtype.RoomType;
import dev.milan.jpasolopractice.yogasession.YogaSession;

import java.time.LocalDate;
import java.time.LocalTime;

public interface RoomUtil {
    boolean canAddSessionToRoom(Room room, YogaSession session) throws BadRequestApiRequestException;

    Room createARoom(LocalDate date, LocalTime openingHours, LocalTime closingHours, RoomType type);

    boolean removeSessionFromRoom(Room room, YogaSession session);

    LocalTime getMinOpeningHours();

    LocalTime getMaxClosingHours();
}
