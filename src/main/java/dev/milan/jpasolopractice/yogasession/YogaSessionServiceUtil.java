package dev.milan.jpasolopractice.yogasession;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ForbiddenApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.person.Person;
import dev.milan.jpasolopractice.room.Room;
import dev.milan.jpasolopractice.roomtype.RoomType;
import dev.milan.jpasolopractice.person.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
public class YogaSessionServiceUtil {

    private final PersonService personService;

    @Autowired
    public YogaSessionServiceUtil(PersonService personService) {
        this.personService = personService;
    }

    public YogaSession createAYogaSession(LocalDate date, RoomType roomType, LocalTime startTime, int duration) throws ApiRequestException {
        YogaSession newOne = new YogaSession();
            setDate(newOne,date);
            setRoomType(newOne, roomType);
            setStartOfSession(newOne,startTime, date);
            setDuration(newOne,duration);
            setEndOfSession(newOne);
            calculateFreeSpace(newOne);
            return newOne;
    }
    private void setRoomType(YogaSession session, RoomType roomType){
        session.setRoomType(roomType);
    }

    private void setDate(YogaSession session, LocalDate date) {
        if (date.isBefore(LocalDate.now())){
            session.setDate(LocalDate.now());
        }else{
            session.setDate(date);
        }
    }

private void setStartOfSession(YogaSession session, LocalTime startOfSession, LocalDate date) throws ApiRequestException{
    if (startOfSession != null){

        if (date.isEqual(LocalDate.now()) && startOfSession.isBefore(LocalTime.now().plus(30, MINUTES))){
            BadRequestApiRequestException.throwBadRequestException("Must reserve a session at least 30 minutes in advance.");
        }
        session.setStartOfSession(startOfSession);
    }else{
        BadRequestApiRequestException.throwBadRequestException("Session must have a start time assigned.");
    }
}


    private void setDuration(YogaSession session, int duration) {
        int MIN_DURATION = 30;
        session.setDuration(Math.max(duration, MIN_DURATION));
    }

    private void setEndOfSession(YogaSession session) {
        if (session.getStartOfSession() != null){
            session.setEndOfSession(session.getStartOfSession().plus(session.getDuration(), ChronoUnit.MINUTES));
        }
    }

    public LocalTime getEndOfSession(YogaSession session){
        if (session.getEndOfSession() == null){
            setEndOfSession(session);
        }
        return session.getEndOfSession();
    }


private boolean addOneBooked(YogaSession session) {
    calculateFreeSpace(session);
    if (session.getFreeSpace() < 1){
        return false;
    }else{
        session.bookOneSpace();
        calculateFreeSpace(session);
        return true;
    }
}

    private void removeOneBooked(YogaSession session){
        calculateFreeSpace(session);
        session.removeOneBooked();
        calculateFreeSpace(session);
    }

    public int getFreeSpace(YogaSession session) {
        return calculateFreeSpace(session);
    }

    private int calculateFreeSpace(YogaSession session){
        session.setFreeSpace(session.getRoomType().getMaxCapacity() - session.getBookedSpace());
        return session.getFreeSpace();
    }

    public boolean addMember(Person person, YogaSession session) throws ConflictApiRequestException {
        if (!containsMember(person,session)){
            if (personService.addSessionToPerson(session,person)){
                if (addOneBooked(session)){
                    session.addMember(person);
                    return true;
                }else{
                    throw ForbiddenApiRequestException.throwForbiddenApiRequestException("Session id:"
                            + session.getId() + " member limit reached.");
                }
            }
        }
        ConflictApiRequestException.throwConflictApiRequestException("User id:" + person.getId() + " already present in session id:" + session.getId());
        return false;
    }

    public boolean removeMember(Person person, YogaSession session) throws NotFoundApiRequestException {
        if(containsMember(person,session)){
            if (personService.removeSessionFromPerson(person,session)){
                session.removeMember(person);
                removeOneBooked(session);
                return true;
            }
        }
        NotFoundApiRequestException.throwNotFoundException("Person id:" + person.getId() + " not found in session id:" + session.getId());
        return false;
    }
    public boolean containsMember(Person person, YogaSession session){
        for (Person p : session.getMembersAttending()){
            if (p.equals(person)){
                return true;
            }
        }
        return false;
    }


    public List<YogaSession> getAllRoomsSessionsInADay(List<Room> rooms) {
        ArrayList<YogaSession> listOfSessions = new ArrayList<>();
        for (Room room : rooms){
            listOfSessions.addAll(room.getSessionList());
        }
        return Collections.unmodifiableList(listOfSessions);
    }
}
