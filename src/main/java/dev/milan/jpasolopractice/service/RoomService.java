package dev.milan.jpasolopractice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.data.RoomRepository;
import dev.milan.jpasolopractice.data.YogaSessionRepository;
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
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomServiceImpl roomServiceImpl;
    private final YogaSessionRepository yogaSessionRepository;
    private final FormatCheckService formatCheckService;
    private final ObjectMapper mapper;

    @Autowired
    public RoomService(RoomRepository roomRepository, RoomServiceImpl roomServiceImpl,YogaSessionRepository yogaSessionRepository
                        , FormatCheckService formatCheckService, ObjectMapper mapper) {
        this.yogaSessionRepository = yogaSessionRepository;
        this.roomRepository = roomRepository;
        this.roomServiceImpl = roomServiceImpl;
        this.formatCheckService = formatCheckService;
        this.mapper = mapper;
    }

    @Transactional
    public Room createARoom(String dateToSave, String openingHoursToSave, String closingHoursToSave, String typeToSave) throws ApiRequestException{
        LocalDate date = formatCheckService.checkDateFormat(dateToSave);
        LocalTime openingHours = formatCheckService.checkTimeFormat(openingHoursToSave);
        LocalTime closingHours = formatCheckService.checkTimeFormat(closingHoursToSave);
        RoomType type = formatCheckService.checkRoomTypeFormat(typeToSave);

        Room found = roomRepository.findRoomByDateAndRoomType(date,type);
        if (found == null){
            Room room = roomServiceImpl.createARoom(date,openingHours,closingHours,type);
            roomRepository.save(room);
           return room;
        }else{
            ConflictApiRequestException.throwConflictApiRequestException("Room id:" + found.getId() + " already exists.");
        }
        return null;
    }

    public Room findRoomById(int id) {
        return  roomRepository.findById(id).orElseThrow(()-> NotFoundApiRequestException.throwNotFoundException("Room with id:" + id + " doesn't exist."));
    }



    @Transactional
    public YogaSession addSessionToRoom(int roomId, int sessionId) throws ApiRequestException{
        Room foundRoom = roomRepository.findById(roomId).orElseThrow(() -> NotFoundApiRequestException.throwNotFoundException("Room id:" + roomId + " not found."));
        YogaSession foundSession = yogaSessionRepository.findById(sessionId).orElseThrow(() -> NotFoundApiRequestException.throwNotFoundException("Yoga session id:" + sessionId + " not found."));

            if(roomServiceImpl.canAddSessionToRoom(foundRoom,foundSession)){
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

        if (roomServiceImpl.removeSessionFromRoom(room, session)){
            roomRepository.save(room);
            yogaSessionRepository.save(session);
            return room;
        }
        NotFoundApiRequestException.throwNotFoundException("Room id:" + room.getId() + " doesn't contain yoga session id:" + session.getId());
        return null;
    }


    public List<Room> findRoomsByTypeAndDate(String datePassed, String roomTypePassed) throws BadRequestApiRequestException {
        LocalDate date = formatCheckService.checkDateFormat(datePassed);
        RoomType type = formatCheckService.checkRoomTypeFormat(roomTypePassed);
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

    public void removeRoom(int roomId) throws NotFoundApiRequestException {
        Optional<Room> room = roomRepository.findById(roomId);
        if (room.isPresent()){
            roomRepository.delete(room.get());
        }else{
            NotFoundApiRequestException.throwNotFoundException("Room id:" + roomId + " not found.");
        }
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
        RoomType type = formatCheckService.checkRoomTypeFormat(s);
        return roomRepository.findRoomsByRoomType(type);
    }

    private List<Room> findAllRoomsBasedOnDate(String s) throws BadRequestApiRequestException{
        LocalDate date = formatCheckService.checkDateFormat(s);
        return roomRepository.findAllRoomsByDate(date);
    }
    @Transactional
    public Room patchRoom(String roomId, JsonPatch patch) throws ApiRequestException {
        Room foundRoom = findRoomById(formatCheckService.checkNumberFormat(roomId));
        Room patchedRoom = applyPatchToRoom(patch,foundRoom);

        List<YogaSession> sessions = patchedRoom.getSessionList();
        int id = patchedRoom.getId();
        patchedRoom = roomServiceImpl.createARoom(formatCheckService.checkDateFormat(patchedRoom.getDate().toString())
                ,formatCheckService.checkTimeFormat(patchedRoom.getOpeningHours().toString())
                ,formatCheckService.checkTimeFormat(patchedRoom.getClosingHours().toString())
                ,formatCheckService.checkRoomTypeFormat(patchedRoom.getRoomType().name()));
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
