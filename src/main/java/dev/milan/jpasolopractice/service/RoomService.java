package dev.milan.jpasolopractice.service;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomServiceImpl roomServiceImpl;
    private final YogaSessionRepository yogaSessionRepository;
    private final FormatCheckService formatCheckService;

    @Autowired
    public RoomService(RoomRepository roomRepository, RoomServiceImpl roomServiceImpl,YogaSessionRepository yogaSessionRepository
                        , FormatCheckService formatCheckService) {
        this.yogaSessionRepository = yogaSessionRepository;
        this.roomRepository = roomRepository;
        this.roomServiceImpl = roomServiceImpl;
        this.formatCheckService = formatCheckService;
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
        Optional<Room> room = roomRepository.findById(id);
        return room.orElseThrow(()-> NotFoundApiRequestException.throwNotFoundException("Room with id:" + id + " doesn't exist."));
    }



    @Transactional
    public YogaSession addSessionToRoom(int roomId, int sessionId) throws ApiRequestException{
        Room foundRoom = roomRepository.findById(roomId).orElseThrow(() -> NotFoundApiRequestException.throwNotFoundException("Room id:" + roomId + " not found."));
        YogaSession foundSession = yogaSessionRepository.findById(sessionId).orElseThrow(() -> NotFoundApiRequestException.throwNotFoundException("Yoga session id:" + sessionId + " not found."));

            if(roomServiceImpl.addSessionToRoom(foundRoom,foundSession)){
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


    public List<YogaSession> getSingleRoomSessionsInADay(int id) throws NotFoundApiRequestException{
        Room room = findRoomById(id);
        if (room != null){
            return Collections.unmodifiableList(room.getSessionList());
        }
        return null;
    }
    public List<YogaSession> getAllRoomsSessionsInADay(String dateString) throws ApiRequestException{
        LocalDate date = formatCheckService.checkDateFormat(dateString);
        List<Room> rooms = roomRepository.findAllRoomsByDate(date);
        if (rooms != null){
            return roomServiceImpl.getAllRoomsSessionsInADay(rooms);
        }else{
            NotFoundApiRequestException.throwNotFoundException("No rooms found on date:" + date);
        }
        return null;
    }

    public List<Room> findRoomByTypeAndDate(String datePassed, String roomTypePassed) throws BadRequestApiRequestException {
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
            return findRoomByTypeAndDate(type.get(),date.get());
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
}
