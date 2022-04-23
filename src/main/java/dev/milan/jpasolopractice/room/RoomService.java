package dev.milan.jpasolopractice.room;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.yogasession.YogaSessionRepository;
import dev.milan.jpasolopractice.roomtype.RoomType;
import dev.milan.jpasolopractice.yogasession.util.SessionInputFormatCheckImpl;
import dev.milan.jpasolopractice.yogasession.YogaSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;


@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomServiceUtil roomServiceUtil;
    private final YogaSessionRepository yogaSessionRepository;
    private final SessionInputFormatCheckImpl sessionInputFormatCheckImpl;
    private final ObjectMapper mapper;

    @Autowired
    public RoomService(RoomRepository roomRepository, RoomServiceUtil roomServiceUtil, YogaSessionRepository yogaSessionRepository
                        , SessionInputFormatCheckImpl sessionInputFormatCheckImpl, ObjectMapper mapper) {
        this.yogaSessionRepository = yogaSessionRepository;
        this.roomRepository = roomRepository;
        this.roomServiceUtil = roomServiceUtil;
        this.sessionInputFormatCheckImpl = sessionInputFormatCheckImpl;
        this.mapper = mapper;
    }

    @Transactional
    public Room createARoom(String dateToSave, String openingHoursToSave, String closingHoursToSave, String typeToSave) throws ApiRequestException{
        LocalDate date = sessionInputFormatCheckImpl.checkDateFormat(dateToSave);
        LocalTime openingHours = sessionInputFormatCheckImpl.checkTimeFormat(openingHoursToSave);
        LocalTime closingHours = sessionInputFormatCheckImpl.checkTimeFormat(closingHoursToSave);
        RoomType type = sessionInputFormatCheckImpl.checkRoomTypeFormat(typeToSave);

        Room found = roomRepository.findRoomByDateAndRoomType(date,type);
        if (found == null){
            Room room = roomServiceUtil.createARoom(date,openingHours,closingHours,type);
            roomRepository.save(room);
           return room;
        }else{
            ConflictApiRequestException.throwConflictApiRequestException("Room id:" + found.getId() + " already exists.");
        }
        return null;
    }

    public Room findRoomById(int id) {
        return  roomRepository.findById(id).orElseThrow(()-> NotFoundApiRequestException.throwNotFoundException("Room id:" + id + " not found."));
    }



    @Transactional
    public YogaSession addSessionToRoom(int roomId, int sessionId) throws ApiRequestException{
        Room foundRoom = roomRepository.findById(roomId).orElseThrow(() -> NotFoundApiRequestException.throwNotFoundException("Room id:" + roomId + " not found."));
        YogaSession foundSession = yogaSessionRepository.findById(sessionId).orElseThrow(() -> NotFoundApiRequestException.throwNotFoundException("Yoga session id:" + sessionId + " not found."));

            if(roomServiceUtil.canAddSessionToRoom(foundRoom,foundSession)){
                foundSession.setRoom(foundRoom);
                foundRoom.addSession(foundSession);
                roomRepository.save(foundRoom);
                yogaSessionRepository.save(foundSession);
                return foundSession;
            }
        return null;
    }

    @Transactional
    public Room removeSessionFromRoom(int roomId, int yogaSessionId) throws ApiRequestException{
        Optional<YogaSession> foundSession = yogaSessionRepository.findById(yogaSessionId);
        YogaSession session = foundSession.orElseThrow(()-> NotFoundApiRequestException.throwNotFoundException("Yoga session with id:" + yogaSessionId + " doesn't exist."));

        Optional<Room> foundRoom = roomRepository.findById(roomId);
        Room room = foundRoom.orElseThrow(()-> NotFoundApiRequestException.throwNotFoundException("Room with id: " + roomId + " doesn't exist."));

        if (roomServiceUtil.removeSessionFromRoom(room, session)){
            roomRepository.save(room);
            yogaSessionRepository.save(session);
            return room;
        }
        NotFoundApiRequestException.throwNotFoundException("Room id:" + room.getId() + " doesn't contain yoga session id:" + session.getId());
        return null;
    }


    public List<Room> findRoomsByTypeAndDate(String datePassed, String roomTypePassed) throws BadRequestApiRequestException {
        LocalDate date = sessionInputFormatCheckImpl.checkDateFormat(datePassed);
        RoomType type = sessionInputFormatCheckImpl.checkRoomTypeFormat(roomTypePassed);
        Room room = roomRepository.findRoomByDateAndRoomType(date,type);
        List<Room> rooms = new ArrayList<>();
        if (room != null){
            rooms.add(room);
        }
        return rooms;
    }

    public List<Room> findAllRooms() {
        List<Room> roomList = (List<Room>) roomRepository.findAll();
        return Collections.unmodifiableList(roomList);
    }
    @Transactional
    public void removeRoom(int roomId) throws NotFoundApiRequestException {
        Room roomToDelete = findRoomById(roomId);
        for (YogaSession session: roomToDelete.getSessionList()){
            session.setRoom(null);
            yogaSessionRepository.save(session);
        }
        roomRepository.delete(roomToDelete);
    }

    public List<Room> findAllRoomsBasedOnParams(Optional<String> date, Optional<String> type) {
        if (date.isEmpty() && type.isEmpty()){
            return findAllRooms();
        }else if (date.isPresent() && type.isPresent()){
            return findRoomsByTypeAndDate(date.get(),type.get());
        }else if(date.isPresent()){
            return findAllRoomsBasedOnDate(date.get());
        }else{
            return findAllRoomsBasedOnRoomType(type.get());
        }
    }

    private List<Room> findAllRoomsBasedOnRoomType(String s) throws BadRequestApiRequestException {
        RoomType type = sessionInputFormatCheckImpl.checkRoomTypeFormat(s);
        return roomRepository.findRoomsByRoomType(type);
    }

    private List<Room> findAllRoomsBasedOnDate(String s) throws BadRequestApiRequestException{
        LocalDate date = sessionInputFormatCheckImpl.checkDateFormat(s);
        return roomRepository.findAllRoomsByDate(date);
    }
    @Transactional
    public Room patchRoom(String roomId, JsonPatch patch) throws ApiRequestException {
        Room foundRoom = findRoomById(sessionInputFormatCheckImpl.checkNumberFormat(roomId));
        Room patchedRoom = applyPatchToRoom(patch,foundRoom);

        List<YogaSession> sessions = patchedRoom.getSessionList();
        int id = patchedRoom.getId();
        patchedRoom = roomServiceUtil.createARoom(sessionInputFormatCheckImpl.checkDateFormat(patchedRoom.getDate().toString())
                , sessionInputFormatCheckImpl.checkTimeFormat(patchedRoom.getOpeningHours().toString())
                , sessionInputFormatCheckImpl.checkTimeFormat(patchedRoom.getClosingHours().toString())
                , sessionInputFormatCheckImpl.checkRoomTypeFormat(patchedRoom.getRoomType().name()));
        patchedRoom.setSessionList(sessions);
        patchedRoom.setId(id);
        return updateRoom(foundRoom,patchedRoom);
    }

    private Room updateRoom(Room foundRoom, Room patchedRoom) {
        if (foundRoom.getId() != patchedRoom.getId()){
            BadRequestApiRequestException.throwBadRequestException("Patch request cannot change room id.");
        }else if(!Arrays.equals(foundRoom.getSessionList().toArray(), patchedRoom.getSessionList().toArray())){
            BadRequestApiRequestException.throwBadRequestException("Patch request cannot change sessions in the room.");
        }else if(!Objects.equals(foundRoom.getRoomType(),patchedRoom.getRoomType())){
            BadRequestApiRequestException.throwBadRequestException("Patch request cannot change room type.");
        }else if(!Objects.equals(foundRoom.getTotalCapacity(),patchedRoom.getTotalCapacity())){
            BadRequestApiRequestException.throwBadRequestException("Patch request cannot directly set total room capacity.");
        }else{
            boolean datesDontMatch = !foundRoom.getDate().equals(patchedRoom.getDate());
            boolean openingTimeDontMatch = !foundRoom.getOpeningHours().equals(patchedRoom.getOpeningHours());
            boolean closingTimeDontMatch = !foundRoom.getClosingHours().equals(patchedRoom.getClosingHours());

            if (datesDontMatch){
                return updateRoomDate(foundRoom,patchedRoom, (openingTimeDontMatch || closingTimeDontMatch));
            }if (openingTimeDontMatch || closingTimeDontMatch){
               return updateRoomTime(foundRoom,patchedRoom);
            }
        }
        return null;
    }

    private Room updateRoomTime(Room foundRoom, Room patchedRoom) {
        List<YogaSession> modifiedSessionList = new ArrayList<>();
        for (YogaSession session: patchedRoom.getSessionList()){
            if (session.getStartOfSession().isBefore(patchedRoom.getOpeningHours()) || session.getEndOfSession().isAfter(patchedRoom.getClosingHours())){
//                patchedRoom.getSessionList().remove(session);
                session.setRoom(null);
                yogaSessionRepository.save(session);
            }else{
                modifiedSessionList.add(session);
            }
        }
        patchedRoom.setSessionList(modifiedSessionList);
        return roomRepository.save(patchedRoom);
    }

    private Room updateRoomDate(Room foundRoom, Room patchedRoom, boolean timesDontMatch) throws ApiRequestException{
        List<Room> cantMoveIfExists = findRoomsByTypeAndDate(patchedRoom.getDate().toString(),patchedRoom.getRoomType().name());
        if (cantMoveIfExists.isEmpty()){
            List<YogaSession> modifiedList = new ArrayList<>();
            for (YogaSession session: patchedRoom.getSessionList()){
                if (timesDontMatch) {
                    if (session.getStartOfSession().isBefore(patchedRoom.getOpeningHours()) || session.getEndOfSession().isAfter(patchedRoom.getClosingHours())) {
                        session.setRoom(null);
                    }else{
                        session.setDate(patchedRoom.getDate());
                        modifiedList.add(session);
                    }
                    yogaSessionRepository.save(session);
                }else{
                    session.setDate(patchedRoom.getDate());
                    modifiedList.add(session);
                    yogaSessionRepository.save(session);
                }

            }
            patchedRoom.setSessionList(modifiedList);
            return roomRepository.save(patchedRoom);
        }else{
            ConflictApiRequestException.throwConflictApiRequestException("Room with date:" + patchedRoom.getDate() + " and room type:"
                    + patchedRoom.getRoomType().name() + " already exists.");
        }
        return null;
    }

    private Room applyPatchToRoom(JsonPatch patch, Room targetRoom) throws BadRequestApiRequestException{
        try{
            JsonNode patched = patch.apply(mapper.convertValue(targetRoom, JsonNode.class));
            return mapper.treeToValue(patched, Room.class);
        } catch (JsonPatchException | JsonProcessingException e) {
            BadRequestApiRequestException.throwBadRequestException("Incorrect patch request data.");
        }
        return targetRoom;
    }
}
