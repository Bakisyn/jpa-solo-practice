package dev.milan.jpasolopractice.room;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.room.util.RoomUtil;
import dev.milan.jpasolopractice.roomtype.RoomType;
import dev.milan.jpasolopractice.shared.Patcher;
import dev.milan.jpasolopractice.yogasession.YogaSession;
import dev.milan.jpasolopractice.yogasession.YogaSessionRepository;
import dev.milan.jpasolopractice.yogasession.util.SessionInputChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomUtil roomUtil;
    private final YogaSessionRepository yogaSessionRepository;
    private final SessionInputChecker sessionInputChecker;
    private final ObjectMapper mapper;
    private final Patcher<Room> patcher;

    @Autowired
    public RoomService(RoomRepository roomRepository, RoomUtil roomUtil, YogaSessionRepository yogaSessionRepository
                        , SessionInputChecker sessionInputChecker, ObjectMapper mapper, Patcher<Room> patcher) {
        this.yogaSessionRepository = yogaSessionRepository;
        this.roomRepository = roomRepository;
        this.roomUtil = roomUtil;
        this.sessionInputChecker = sessionInputChecker;
        this.mapper = mapper;
        this.patcher = patcher;
    }

    @Transactional
    public Room createARoom(String dateToSave, String openingHoursToSave, String closingHoursToSave, String typeToSave) throws ApiRequestException{
        LocalDate date = sessionInputChecker.checkDateFormat(dateToSave);
        LocalTime openingHours = sessionInputChecker.checkTimeFormat(openingHoursToSave);
        LocalTime closingHours = sessionInputChecker.checkTimeFormat(closingHoursToSave);
        RoomType type = sessionInputChecker.checkRoomTypeFormat(typeToSave);

        Room found = roomRepository.findSingleRoomByDateAndType(date,type);
        if (found == null){
            Room room = roomUtil.createARoom(date,openingHours,closingHours,type);
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

            if(roomUtil.canAddSessionToRoom(foundRoom,foundSession)){
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

        if (roomUtil.removeSessionFromRoom(room, session)){
            roomRepository.save(room);
            yogaSessionRepository.save(session);
            return room;
        }
        NotFoundApiRequestException.throwNotFoundException("Room id:" + room.getId() + " doesn't contain yoga session id:" + session.getId());
        return null;
    }


    public List<Room> findRoomsByTypeAndDate(String datePassed, String roomTypePassed) throws BadRequestApiRequestException {
        LocalDate date = sessionInputChecker.checkDateFormat(datePassed);
        RoomType type = sessionInputChecker.checkRoomTypeFormat(roomTypePassed);
        Room room = roomRepository.findSingleRoomByDateAndType(date,type);
        List<Room> rooms = new ArrayList<>();
        if (room != null){
            rooms.add(room);
        }
        return rooms;
    }
    public Room findSingleRoomByDateAndType(LocalDate date , RoomType roomType)  throws BadRequestApiRequestException{
        Room room = roomRepository.findSingleRoomByDateAndType(date, roomType);
        if (room == null){
            NotFoundApiRequestException.throwNotFoundException("Room with type:" + date + " not found on date: " + roomType);
        }
        return room;
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
        RoomType type = sessionInputChecker.checkRoomTypeFormat(s);
        return roomRepository.findRoomsByRoomType(type);
    }

    private List<Room> findAllRoomsBasedOnDate(String s) throws BadRequestApiRequestException{
        LocalDate date = sessionInputChecker.checkDateFormat(s);
        return roomRepository.findAllRoomsByDate(date);
    }

        @Transactional
    public Room patchRoom(String roomId, JsonPatch patch) throws ApiRequestException {
        Room foundRoom = findRoomById(sessionInputChecker.checkNumberFormat(roomId));
        return patcher.patch(patch,foundRoom);
    }
}
