package dev.milan.jpasolopractice.yogasession.util;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ForbiddenApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.person.Person;
import dev.milan.jpasolopractice.person.PersonService;
import dev.milan.jpasolopractice.roomtype.RoomType;
import dev.milan.jpasolopractice.yogasession.YogaSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
public class YogaSessionUtilImpl implements YogaSessionUtil {

    private final int MIN_DURATION = 30;
    private final LocalTime LATEST_SESSION_ENDING = LocalTime.of(23,59,59);
    private final int RESERVE_IN_ADVANCE = 30;
    private final LocalTime LATEST_RESERVATION = LATEST_SESSION_ENDING.minusMinutes(MIN_DURATION).minusMinutes(RESERVE_IN_ADVANCE);
    private final PersonService personService;

    @Autowired
    public YogaSessionUtilImpl(PersonService personService) {
        this.personService = personService;
    }

    @Override
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

        if (date.isEqual(LocalDate.now()) && startOfSession.isBefore(LocalTime.now().plusMinutes(RESERVE_IN_ADVANCE))){
            BadRequestApiRequestException.throwBadRequestException("Must reserve a session at least "+ RESERVE_IN_ADVANCE +" minutes in advance.");
        }
        if (date.isEqual(LocalDate.now()) &&
                (LocalTime.now().isAfter(LATEST_RESERVATION))){
            BadRequestApiRequestException.throwBadRequestException("Can't reserve a session on date:" + date + " after time:" + LATEST_RESERVATION);
        }
        session.setStartOfSession(startOfSession);
    }else{
        BadRequestApiRequestException.throwBadRequestException("Session must have a start time assigned.");
    }
}


    private void setDuration(YogaSession session, int duration) throws BadRequestApiRequestException{
        if (duration < MIN_DURATION){
            BadRequestApiRequestException.throwBadRequestException("Session duration cannot be less than " + MIN_DURATION);
        }
        session.setDuration(duration);
    }

    private void setEndOfSession(YogaSession session) throws BadRequestApiRequestException {
        if (session.getStartOfSession() != null){

            if (MINUTES.between(session.getStartOfSession(), LATEST_SESSION_ENDING) < session.getDuration()){
                BadRequestApiRequestException.throwBadRequestException("Ending time must be at the before or equal to " + LATEST_SESSION_ENDING);
            }
            session.setEndOfSession(session.getStartOfSession().plus(session.getDuration(), ChronoUnit.MINUTES));
        }
    }

    @Override
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

    @Override
    public int getFreeSpace(YogaSession session) {
        return calculateFreeSpace(session);
    }

    private int calculateFreeSpace(YogaSession session){
        session.setFreeSpace(session.getRoomType().getMaxCapacity() - session.getBookedSpace());
        return session.getFreeSpace();
    }

    @Override
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

    @Override
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
    @Override
    public boolean containsMember(Person person, YogaSession session){
        for (Person p : session.getMembersAttending()){
            if (p.equals(person)){
                return true;
            }
        }
        return false;
    }


    @Override
    public int getMinDuration() {
        return MIN_DURATION;
    }

    @Override
    public LocalTime getLatestSessionEnding() {
        return LATEST_SESSION_ENDING;
    }

    @Override
    public int getReserveInAdvanceAmount() {
        return RESERVE_IN_ADVANCE;
    }

    @Override
    public LocalTime getLatestReservation() {
        return LATEST_RESERVATION;
    }
}
