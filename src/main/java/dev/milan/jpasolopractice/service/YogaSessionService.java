package dev.milan.jpasolopractice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ForbiddenApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.data.PersonRepository;
import dev.milan.jpasolopractice.data.RoomRepository;
import dev.milan.jpasolopractice.data.YogaSessionRepository;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.RoomType;
import dev.milan.jpasolopractice.model.YogaSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;


@Service
public class YogaSessionService {

    private final YogaSessionServiceUtil sessionServiceImpl;
    private final YogaSessionRepository yogaSessionRepository;
    private final  PersonRepository personRepository;
    private final FormatCheckService formatCheckService;
    private final RoomRepository roomRepository;
    private final ObjectMapper mapper;
    private final RoomServiceUtil roomServiceUtil;
    private final RoomService roomService;
        @Autowired
        public YogaSessionService(YogaSessionServiceUtil sessionServiceImpl, YogaSessionRepository yogaSessionRepository, PersonRepository personRepository
                                    , FormatCheckService formatCheckService, RoomRepository roomRepository, ObjectMapper mapper, RoomServiceUtil roomServiceUtil, RoomService roomService) {
        this.sessionServiceImpl = sessionServiceImpl;
        this.yogaSessionRepository = yogaSessionRepository;
        this.personRepository = personRepository;
        this.formatCheckService = formatCheckService;
        this.roomRepository = roomRepository;
        this.mapper = mapper;
        this.roomServiceUtil = roomServiceUtil;
        this.roomService = roomService;
    }


    @Transactional
    public YogaSession createAYogaSession(String dateString, String roomTypeString, String startTimeString, String durationString) throws ApiRequestException {
        if (dateString == null || roomTypeString == null || startTimeString == null || durationString == null){
            BadRequestApiRequestException.throwBadRequestException("Date, room type, start time and duration must have values assigned.");
            }
        LocalDate date = formatCheckService.checkDateFormat(dateString);
        RoomType roomType = formatCheckService.checkRoomTypeFormat(roomTypeString);
        LocalTime startTime = formatCheckService.checkTimeFormat(startTimeString);
        int duration = formatCheckService.checkNumberFormat(durationString);

        YogaSession found = yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoomType(date,startTime,roomType);
        if (found == null){
            found = sessionServiceImpl.createAYogaSession(date,roomType,startTime,duration);
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

            if(sessionServiceImpl.addMember(foundPerson,foundSession)){
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

                if(sessionServiceImpl.removeMember(foundPerson,foundSession)){
                yogaSessionRepository.save(foundSession);
                personRepository.save(foundPerson);
                return true;
                }
                return false;
    }
    private Person findUserById(int userId){
        return personRepository.findById(userId).orElseThrow(() -> new NotFoundApiRequestException("Person id:" + userId + " couldn't be found."));
    }

    public LocalTime getEndOfSession(YogaSession session) {
       return sessionServiceImpl.getEndOfSession(session);
    }

    public int getFreeSpace(YogaSession session) {
        Optional<YogaSession> found = Optional.ofNullable(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoomType(session.getDate(), session.getStartOfSession(), session.getRoomType()));
        return found.map(sessionServiceImpl::getFreeSpace).orElse(-1);
    }

    public YogaSession findYogaSessionById(int yogaSessionId) {
        return yogaSessionRepository.findById(yogaSessionId).orElseThrow(()-> NotFoundApiRequestException.throwNotFoundException("Yoga session id:" + yogaSessionId +  " not found."));
        }

    public List<YogaSession> findAllSessions() {
            return (List<YogaSession>) yogaSessionRepository.findAll();
    }

    public List<YogaSession> getAllRoomsSessionsInADay(String dateString) throws ApiRequestException{
        LocalDate date = formatCheckService.checkDateFormat(dateString);
        List<Room> rooms = roomRepository.findAllRoomsByDate(date);
        if (rooms != null){
            return sessionServiceImpl.getAllRoomsSessionsInADay(rooms);
        }else{
            NotFoundApiRequestException.throwNotFoundException("No rooms found on date:" + date);
        }
        return null;
    }

    public List<YogaSession> getSingleRoomSessionsInADay(int id) throws NotFoundApiRequestException{
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
                        return findSessionsInAllRoomsWithDate(formatCheckService.checkDateFormat(dateString.get()));
                    }else{
                        return findSessionsInAllRooms();
                    }
                }else if(typeString.get().equalsIgnoreCase("none")){
                    if (dateString.isPresent()){
                        return findSessionsWithoutRoomsWithDate(formatCheckService.checkDateFormat(dateString.get()));
                    }else{
                        return findSessionsWithNoRoom();
                    }
                }else{
                   RoomType roomType =  formatCheckService.checkRoomTypeFormat(typeString.get());
                   if (dateString.isPresent()){
                       return findSessionsInRoomsWithTypeAndDate(roomType, formatCheckService.checkDateFormat(dateString.get()));
                   }else{
                       return findSessionsInAllRoomsWithType(roomType);
                   }
                }
            }else{
                if (dateString.isPresent()){
                    LocalDate date = formatCheckService.checkDateFormat(dateString.get());
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
            YogaSession sessionFound = findYogaSessionById(formatCheckService.checkNumberFormat(id));
            YogaSession patchedSession = applyPatchToSession(patch, sessionFound);
            return updateSession(sessionFound, patchedSession);
    }
    private YogaSession updateSession(YogaSession sessionFound, YogaSession patchedSession) {
            if (sessionFound.getId() != patchedSession.getId()){
                BadRequestApiRequestException.throwBadRequestException("Patch request cannot change session id.");
            }else if(!Arrays.equals(sessionFound.getMembersAttending().toArray(), patchedSession.getMembersAttending().toArray())){
                BadRequestApiRequestException.throwBadRequestException("Patch request cannot change session members.");
            }else if(!Objects.equals(sessionFound.getRoom(),patchedSession.getRoom())){
                BadRequestApiRequestException.throwBadRequestException("Patch request cannot directly assign a room.");
            }else if(!sessionFound.getEndOfSession().equals(patchedSession.getEndOfSession())){
                BadRequestApiRequestException.throwBadRequestException("Patch request cannot directly set end of session. Pass start time and duration of session.");
            }else if(!(sessionFound.getBookedSpace() == patchedSession.getBookedSpace())){
                BadRequestApiRequestException.throwBadRequestException("Patch request cannot directly set booked space.");
            }else if(!(sessionFound.getFreeSpace() == patchedSession.getFreeSpace())){
                BadRequestApiRequestException.throwBadRequestException("Patch request cannot directly set free space.");
            }else {
                boolean roomTypesDontMatch = !patchedSession.getRoomType().equals(sessionFound.getRoomType());
                boolean datesDontMatch = !patchedSession.getDate().equals(sessionFound.getDate());
                boolean startOfSessionDontMatch = !patchedSession.getStartOfSession().equals(sessionFound.getStartOfSession());
                boolean durationDontMatch = !(patchedSession.getDuration() == sessionFound.getDuration());
                if (roomTypesDontMatch || datesDontMatch){
                   return updateSessionRoomTypeOrDateIfPossible(sessionFound, patchedSession);  //za oba ova trebam da napravim i sta se
                }else{                                                                          //desava ako session nije u room
                    if (startOfSessionDontMatch || durationDontMatch){
                        return updateSessionStartTimeOrDuration(sessionFound, patchedSession);
                    }
                }

            }
        return null;
    }
    private YogaSession updateSessionStartTimeOrDuration(YogaSession sessionFound, YogaSession patchedSession)  throws ApiRequestException{
            Room room = sessionFound.getRoom();
            if (room != null){
                room.getSessionList().remove(sessionFound);
                patchedSession = setUpASessionForRoomOrDateChange(sessionFound, patchedSession);
                patchedSession.setRoom(null);
                if (roomServiceUtil.canAddSessionToRoom(room, patchedSession)){
                    room.getSessionList().add(sessionFound);
                    return replaceSessionForModifiedOneAndSave(sessionFound, patchedSession, room);
                }
            }else{
                return changeSessionWithoutARoom(patchedSession);
            }
        return patchedSession;
    }


    private YogaSession updateSessionRoomTypeOrDateIfPossible(YogaSession sessionFound, YogaSession patchedSession) throws ApiRequestException{
            if (patchedSession.getRoomType().getMaxCapacity() < patchedSession.getBookedSpace()){
                ForbiddenApiRequestException
                        .throwForbiddenApiRequestException("Cannot change room type to a type with capacity lower than number of members in yoga session.");
            }
            Room fromRoom = patchedSession.getRoom();
            if (fromRoom != null){
                Room room = findRoomByDateAndTime(patchedSession.getDate().toString(),patchedSession.getRoomType().name());
                patchedSession = setUpASessionForRoomOrDateChange(sessionFound, patchedSession);
                patchedSession.setRoom(null);
                if (roomServiceUtil.canAddSessionToRoom(room, patchedSession)){
                    return replaceSessionForModifiedOneAndSave(sessionFound, patchedSession, room);
                }
            }else{
                return changeSessionWithoutARoom(patchedSession);
            }

        return null;
    }

    private YogaSession replaceSessionForModifiedOneAndSave(YogaSession sessionFound, YogaSession patchedSession, Room room) {
        roomService.removeSessionFromRoom(sessionFound.getRoom().getId(), sessionFound.getId());
        yogaSessionRepository.save(patchedSession);
        patchedSession.setRoom(room);
        room.addSession(patchedSession);
        roomRepository.save(room);
        yogaSessionRepository.save(patchedSession);
        return patchedSession;
    }

    private YogaSession setUpASessionForRoomOrDateChange(YogaSession sessionFound, YogaSession patchedSession) {
        patchedSession = sessionServiceImpl.createAYogaSession(patchedSession.getDate(),patchedSession.getRoomType()
                ,patchedSession.getStartOfSession(),patchedSession.getDuration());
        patchedSession.setId(sessionFound.getId());
        patchedSession.setMembersAttending(sessionFound.getMembersAttending());
        return patchedSession;
    }
    private YogaSession changeSessionWithoutARoom(YogaSession session) throws ApiRequestException{
        int id = session.getId();
        List<Person> members = session.getMembersAttending();
        session = sessionServiceImpl.createAYogaSession(formatCheckService.checkDateFormat(session.getDate().toString())
                ,formatCheckService.checkRoomTypeFormat(session.getRoomType().name()),formatCheckService.checkTimeFormat(session.getStartOfSession().toString())
                ,formatCheckService.checkNumberFormat("" + session.getDuration()));
        session.setId(id);
        session.setMembersAttending(members);
        return yogaSessionRepository.save(session);
    }


    private YogaSession applyPatchToSession(JsonPatch patch, YogaSession targetSession) throws BadRequestApiRequestException{
            try{
                JsonNode patched = patch.apply(mapper.convertValue(targetSession, JsonNode.class));
                YogaSession toReturn = mapper.treeToValue(patched, YogaSession.class);
                return toReturn;
            } catch (JsonPatchException | JsonProcessingException e) {
                BadRequestApiRequestException.throwBadRequestException("Incorrect patch request data.");
            }
            return targetSession;
    }

    private Room findRoomByDateAndTime(String date , String roomType)  throws BadRequestApiRequestException{
        Room room = roomRepository.findRoomByDateAndRoomType(formatCheckService.checkDateFormat(date)
                ,formatCheckService.checkRoomTypeFormat(roomType));
        if (room == null){
            NotFoundApiRequestException.throwNotFoundException("Room with type:" + date + " not found on date: " + roomType);
        }
        return room;
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
