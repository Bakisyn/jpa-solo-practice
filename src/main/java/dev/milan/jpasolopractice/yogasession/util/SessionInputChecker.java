package dev.milan.jpasolopractice.yogasession.util;

import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.roomtype.RoomType;

import java.time.LocalDate;
import java.time.LocalTime;

public interface SessionInputChecker {

    LocalDate checkDateFormat(String dateToSave) throws BadRequestApiRequestException;

    LocalTime checkTimeFormat(String timeString) throws BadRequestApiRequestException;

    RoomType checkRoomTypeFormat(String yogaRoomType) throws BadRequestApiRequestException;

    Integer checkNumberFormat(String number) throws BadRequestApiRequestException;
}
