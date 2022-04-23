package dev.milan.jpasolopractice.yogasession.util;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.person.Person;
import dev.milan.jpasolopractice.roomtype.RoomType;
import dev.milan.jpasolopractice.yogasession.YogaSession;

import java.time.LocalDate;
import java.time.LocalTime;

public interface YogaSessionUtil {
    YogaSession createAYogaSession(LocalDate date, RoomType roomType, LocalTime startTime, int duration) throws ApiRequestException;

    boolean addMember(Person person, YogaSession session) throws ConflictApiRequestException;

    boolean removeMember(Person person, YogaSession session) throws NotFoundApiRequestException;

    boolean containsMember(Person person, YogaSession session);

    int getMinDuration();

    LocalTime getLatestSessionEnding();

    int getReserveInAdvanceAmount();

    LocalTime getLatestReservation();
}
