package dev.milan.jpasolopractice.room.util;

import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.room.Room;
import dev.milan.jpasolopractice.roomtype.RoomType;
import dev.milan.jpasolopractice.yogasession.YogaSession;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class RoomUtilImpl implements RoomUtil {
    private final LocalTime MIN_OPENING_HOURS = LocalTime.of(6,0,0);
    private final LocalTime MAX_CLOSING_HOURS = LocalTime.of(23,0,0);

    @Override
    public boolean canAddSessionToRoom(Room room, YogaSession session) throws BadRequestApiRequestException{
        return addSessionToRoomIfPossible(room,session);
    }

    private boolean addSessionToRoomIfPossible(Room room, YogaSession session) throws BadRequestApiRequestException{
            if (session.getRoom() == null){
                if (session.getRoomType().equals(room.getRoomType())){
                    if(session.getDate().isEqual(room.getDate())){
                        if (checkIfWantedSessionTimeIsAvailable(room,session)){
                            return true;
                        }else{
                            BadRequestApiRequestException.throwBadRequestException("Yoga session time period is already occupied.");
                        }

                    }else{
                        BadRequestApiRequestException.throwBadRequestException("Yoga session must have the same date as room.");
                    }
                }else{
                    BadRequestApiRequestException.throwBadRequestException("Yoga session and room must have a matching room type.");
                }
        }
            BadRequestApiRequestException.throwBadRequestException("Yoga session already has room assigned.");
        return false;
    }

    private boolean checkIfWantedSessionTimeIsAvailable(Room room, YogaSession session) {
        if (session.getStartOfSession().isBefore(room.getOpeningHours())){
            BadRequestApiRequestException.throwBadRequestException("Yoga session cannot start before room opening hours. Room opens at:" + room.getOpeningHours());
        }else if (session.getEndOfSession().isAfter(room.getClosingHours())){
            BadRequestApiRequestException.throwBadRequestException("Yoga session cannot end after room closing hours. Room closes at:" + room.getClosingHours());
        }
        if (room.getSessionList().isEmpty()){
            return true;
        }

        for (YogaSession ses: room.getSessionList()){
            if(ses.getStartOfSession().equals(session.getStartOfSession()) ||  //3&4
                    ses.getEndOfSession().equals(session.getEndOfSession())) {
                return false;
            }

            if(ses.getStartOfSession().isBefore(session.getEndOfSession()) &&  //1
                    ses.getEndOfSession().isAfter(session.getStartOfSession())) {
                return false;
            }

            if(ses.getStartOfSession().isBefore(session.getEndOfSession()) && //2
                    ses.getEndOfSession().isAfter(session.getEndOfSession())) {
                return false;
            }

            if(ses.getStartOfSession().isAfter(session.getStartOfSession()) && //3
                    ses.getEndOfSession().isBefore(session.getEndOfSession())) {
                return false;
            }

        }
        return true;
    }

    @Override
    public Room createARoom(LocalDate date, LocalTime openingHours, LocalTime closingHours, RoomType type){
        Room room = new Room();
        setDate(room,date);
        setOpeningHours(room,openingHours);
        setClosingHours(room,closingHours);
        room.setRoomType(type);
        room.setTotalCapacity(type.getMaxCapacity());
        return room;
    }
    private void setDate(Room room, LocalDate newDate) throws BadRequestApiRequestException{
        if (LocalDate.now().isBefore(newDate) || LocalDate.now().isEqual(newDate)){
            room.setDate(newDate);
        }else{
            BadRequestApiRequestException.throwBadRequestException("Date cannot be before current date.");
        }
    }


    private void setOpeningHours(Room room, LocalTime openingHours) {
        if (openingHours.isBefore(MIN_OPENING_HOURS)){
            BadRequestApiRequestException.throwBadRequestException("Cannot set opening hours before " + MIN_OPENING_HOURS);
        }else if(openingHours.equals(room.getClosingHours())){
            BadRequestApiRequestException.throwBadRequestException("Opening hours and closing hours can't be the same.");
        }else if(openingHours.isAfter(room.getClosingHours())){
            BadRequestApiRequestException.throwBadRequestException("Cannot set opening hours after closing hours.");
        }else{
            room.setOpeningHours(openingHours);
        }
    }


    private void setClosingHours(Room room, LocalTime closingHours) {
        if (closingHours.isAfter(MAX_CLOSING_HOURS)){
            BadRequestApiRequestException.throwBadRequestException("Cannot set closing hours after " + MAX_CLOSING_HOURS);
        }else if(closingHours.equals(room.getOpeningHours())){
            BadRequestApiRequestException.throwBadRequestException("Opening hours and closing hours can't be the same.");
        }else if(closingHours.isBefore(room.getOpeningHours())){
            BadRequestApiRequestException.throwBadRequestException("Cannot set closing hours before opening hours.");
        }else{
            room.setClosingHours(closingHours);
        }
    }

    @Override
    public boolean removeSessionFromRoom(Room room, YogaSession session) {
        if (room.getSessionList().contains(session)){
            room.getSessionList().remove(session);
            session.setRoom(null);
            return true;
        }
            return false;
    }

    @Override
    public LocalTime getMinOpeningHours() {
        return MIN_OPENING_HOURS;
    }

    @Override
    public LocalTime getMaxClosingHours() {
        return MAX_CLOSING_HOURS;
    }

}