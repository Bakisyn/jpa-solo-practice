package dev.milan.jpasolopractice.yogasession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.person.Person;
import dev.milan.jpasolopractice.person.PersonRepository;
import dev.milan.jpasolopractice.room.Room;
import dev.milan.jpasolopractice.room.RoomRepository;
import dev.milan.jpasolopractice.room.RoomService;
import dev.milan.jpasolopractice.room.util.RoomUtil;
import dev.milan.jpasolopractice.roomtype.RoomType;
import dev.milan.jpasolopractice.shared.Patcher;
import dev.milan.jpasolopractice.yogasession.util.SessionInputChecker;
import dev.milan.jpasolopractice.yogasession.util.YogaSessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
public class YogaSessionService {

    private final YogaSessionUtil yogaSessionUtil;
    private final YogaSessionRepository yogaSessionRepository;
    private final  PersonRepository personRepository;
    private final SessionInputChecker sessionInputChecker;
    private final RoomRepository roomRepository;
    private final ObjectMapper mapper;
    private final RoomUtil roomUtil;
    private final RoomService roomService;
    @Qualifier("yogaPatcher")
    private final Patcher<YogaSession> patcher;
        @Autowired
        public YogaSessionService(YogaSessionUtil yogaSessionUtil, YogaSessionRepository yogaSessionRepository, PersonRepository personRepository
                                    , SessionInputChecker sessionInputChecker, RoomRepository roomRepository, ObjectMapper mapper, RoomUtil roomUtil,
                                  RoomService roomService, Patcher<YogaSession> patcher) {
        this.yogaSessionUtil = yogaSessionUtil;
        this.yogaSessionRepository = yogaSessionRepository;
        this.personRepository = personRepository;
        this.sessionInputChecker = sessionInputChecker;
        this.roomRepository = roomRepository;
        this.mapper = mapper;
        this.roomUtil = roomUtil;
        this.roomService = roomService;
        this.patcher = patcher;
    }


    @Transactional
    public YogaSession createAYogaSession(String dateString, String roomTypeString, String startTimeString, String durationString) throws ApiRequestException {
        LocalDate date = sessionInputChecker.checkDateFormat(dateString);
        RoomType roomType = sessionInputChecker.checkRoomTypeFormat(roomTypeString);
        LocalTime startTime = sessionInputChecker.checkTimeFormat(startTimeString);
        int duration = sessionInputChecker.checkNumberFormat(durationString);

        YogaSession found = yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoomType(date,startTime,roomType);
        if (found == null){
            found = yogaSessionUtil.createAYogaSession(date,roomType,startTime,duration);
            yogaSessionRepository.save(found);
        }else{
            ConflictApiRequestException.throwConflictApiRequestException("Yoga session with same date,start time and room type already exists.");
        }
        return found;
    }

    @Transactional
    public boolean addMemberToYogaSession(int sessionId, int userId) throws  ApiRequestException{
            YogaSession foundSession = findYogaSessionById(sessionId);
            Person foundPerson = findUserById(userId);

            if(yogaSessionUtil.addMember(foundPerson,foundSession)){
                personRepository.save(foundPerson);
                yogaSessionRepository.save(foundSession);
                return true;
            }
            return false;
    }
    @Transactional
    public boolean removeMemberFromYogaSession(int sessionId, int personId){
        YogaSession foundSession = findYogaSessionById(sessionId);
        Person foundPerson = findUserById(personId);

                if(yogaSessionUtil.removeMember(foundPerson,foundSession)){
                yogaSessionRepository.save(foundSession);
                personRepository.save(foundPerson);
                return true;
                }
                return false;
    }
    private Person findUserById(int userId){
        return personRepository.findById(userId).orElseThrow(() -> new NotFoundApiRequestException("Person id:" + userId + " couldn't be found."));
    }

    public YogaSession findYogaSessionById(int yogaSessionId) {
        return yogaSessionRepository.findById(yogaSessionId).orElseThrow(()-> NotFoundApiRequestException.throwNotFoundException("Yoga session id:" + yogaSessionId +  " not found."));
        }

    public List<YogaSession> findAllSessions() {
            return (List<YogaSession>) yogaSessionRepository.findAll();
    }

    public List<YogaSession> findAllSessionsInAllRoomsByDate(String dateString) throws ApiRequestException{
        LocalDate date = sessionInputChecker.checkDateFormat(dateString);
        List<Room> rooms = roomRepository.findAllRoomsByDate(date);
        if (rooms != null){
            return findAllSessionsInAllRoomsByDate(rooms);
        }else{
            NotFoundApiRequestException.throwNotFoundException("No rooms found on date:" + date);
        }
        return null;
    }
    private List<YogaSession> findAllSessionsInAllRoomsByDate(List<Room> rooms) {
        ArrayList<YogaSession> listOfSessions = new ArrayList<>();
        for (Room room : rooms){
            listOfSessions.addAll(room.getSessionList());
        }
        return Collections.unmodifiableList(listOfSessions);
    }

    public List<YogaSession> findSingleRoomSessionsInADay(int id) throws NotFoundApiRequestException{
        Room room = roomRepository.findById(id).orElseThrow(()-> NotFoundApiRequestException.throwNotFoundException("Room id:" + id + " not found"));
        if (room != null){
            return Collections.unmodifiableList(room.getSessionList());
        }
        return null;
    }

    public List<YogaSession> findSessionsByParams(Optional<String> dateString, Optional<String> typeString)throws ApiRequestException{

            if (typeString.isPresent()){
                if (typeString.get().equalsIgnoreCase("all")){
                    if (dateString.isPresent()){
                        return findSessionsInAllRoomsWithDate(sessionInputChecker.checkDateFormat(dateString.get()));
                    }else{
                        return findSessionsInAllRooms();
                    }
                }else if(typeString.get().equalsIgnoreCase("none")){
                    if (dateString.isPresent()){
                        return findSessionsWithoutRoomsWithDate(sessionInputChecker.checkDateFormat(dateString.get()));
                    }else{
                        return findSessionsWithNoRoom();
                    }
                }else{
                   RoomType roomType =  sessionInputChecker.checkRoomTypeFormat(typeString.get());
                   if (dateString.isPresent()){
                       return findSessionsInRoomsWithTypeAndDate(roomType, sessionInputChecker.checkDateFormat(dateString.get()));
                   }else{
                       return findSessionsInAllRoomsWithType(roomType);
                   }
                }
            }else{
                if (dateString.isPresent()){
                    LocalDate date = sessionInputChecker.checkDateFormat(dateString.get());
                    return findSessionsInAllRoomsWithDate(date);
                }else{
                    return findAllSessions();
                }
            }
    }

    private List<YogaSession> findSessionsWithNoRoom() {
            return yogaSessionRepository.findYogaSessionByRoomIsNull();
    }

    private List<YogaSession> findSessionsWithoutRoomsWithDate(LocalDate date) {
            return yogaSessionRepository.findYogaSessionByDateAndRoomIsNull(date);
    }

    private List<YogaSession> findSessionsInAllRoomsWithDate(LocalDate date) {
            return yogaSessionRepository.findYogaSessionByDateAndRoomIsNotNull(date);
    }


    private List<YogaSession> findSessionsInRoomsWithTypeAndDate(RoomType roomType, LocalDate date) {
        return yogaSessionRepository.findYogaSessionByRoomTypeAndDateAndRoomIsNotNull(roomType,date);
    }

    private List<YogaSession> findSessionsInAllRooms() {
        return yogaSessionRepository.findYogaSessionByRoomIsNotNull();
    }

    private List<YogaSession> findSessionsInAllRoomsWithType(RoomType checkRoomTypeFormat) {
        return yogaSessionRepository.findYogaSessionByRoomTypeAndRoomIsNotNull(checkRoomTypeFormat);
    }

    @Transactional
    public YogaSession patchSession(String id, JsonPatch patch) throws ApiRequestException{
        YogaSession sessionFound = findYogaSessionById(sessionInputChecker.checkNumberFormat(id));
        return patcher.patch(patch,sessionFound);
    }

    @Transactional
    public void deleteASession(int id) {
            YogaSession session = findYogaSessionById(id);
            for (Person person: session.getMembersAttending()){
                person.getYogaSessions().remove(session);
                personRepository.save(person);
            }
            Room room = session.getRoom();
            if (room != null){
                room.getSessionList().remove(session);
                roomRepository.save(room);
            }
            yogaSessionRepository.delete(session);
    }
}
