package dev.milan.jpasolopractice.service;

import dev.milan.jpasolopractice.customException.SessionNotAvailableException;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.YogaSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
public class YogaSessionServiceImpl {

    private final PersonService personService;
    @Autowired
    public YogaSessionServiceImpl(PersonService personService) {
        this.personService = personService;
    }

    public YogaSession createAYogaSession(LocalDate date, Room room, LocalTime startTime, int duration) throws SessionNotAvailableException{
        YogaSession newOne = new YogaSession();
            setDate(newOne,date);
            setRoom(newOne, room);
            setStartOfSession(newOne,startTime);
            setDuration(newOne,duration);
            setEndOfSession(newOne);
            calculateFreeSpace(newOne);
            return newOne;
    }
    private void setRoom(YogaSession session, Room room){
        session.setRoom(room);
    }

    private void setDate(YogaSession session, LocalDate date) {
        if (date.isBefore(LocalDate.now())){
            session.setDate(LocalDate.now());
        }else{
            session.setDate(date);
        }
    }

private void setStartOfSession(YogaSession session, LocalTime startOfSession) throws SessionNotAvailableException{
    if (session.getRoom() != null && startOfSession != null){
        if (startOfSession.isBefore(session.getRoom().getOpeningHours())){
            throw new SessionNotAvailableException("Yoga sessions start at: " + session.getRoom().getOpeningHours());
        }
        if (startOfSession.isBefore(LocalTime.now().plus(30, MINUTES))){
            throw new SessionNotAvailableException("Must reserve a session at least 30 minutes in advance.");
        }
        session.setStartOfSession(startOfSession);
    }else{
        throw new SessionNotAvailableException("Session must have a room and session start time assigned.");
    }
}


    private void setDuration(YogaSession session, int duration) {
        session.setDuration(Math.max(duration, 30));
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
        if (session.getRoom() == null || session.getFreeSpace() < 1){
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
        session.setFreeSpace(session.getRoom().getTotalCapacity() - session.getBookedSpace());
        return session.getFreeSpace();
    }

    public boolean addMember(Person person,YogaSession session) {
        if (!containsMember(person,session)){
            if (addOneBooked(session)){
                session.addMember(person);
                return true;
            }
        }
        return false;
    }

    public boolean removeMember(Person person,YogaSession session) {
        if(containsMember(person,session)){
            if (personService.removeSessionFromPerson(person.getId(),session.getId())){
                session.removeMember(person);
                removeOneBooked(session);
                return true;
            }
        }
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


}
