package dev.milan.jpasolopractice.service;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.data.RoomRepository;
import dev.milan.jpasolopractice.data.YogaSessionRepository;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.YogaRooms;
import dev.milan.jpasolopractice.model.YogaSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;


@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomServiceImpl roomServiceImpl;
    private final YogaSessionRepository yogaSessionRepository;

    @Autowired
    public RoomService(RoomRepository roomRepository, RoomServiceImpl roomServiceImpl,YogaSessionRepository yogaSessionRepository) {
        this.yogaSessionRepository = yogaSessionRepository;
        this.roomRepository = roomRepository;
        this.roomServiceImpl = roomServiceImpl;
    }

    @Transactional
    public Room createARoom(String dateToSave, String openingHoursToSave, String closingHoursToSave, String typeToSave) throws ApiRequestException{
        LocalDate date = roomServiceImpl.checkDateFormat(dateToSave);
        LocalTime openingHours = roomServiceImpl.checkTimeFormat(openingHoursToSave);
        LocalTime closingHours = roomServiceImpl.checkTimeFormat(closingHoursToSave);
        YogaRooms type = roomServiceImpl.checkRoomTypeFormat(typeToSave);

        Room found = findRoomByRoomTypeAndDate(type,date);
        if (found == null){
            Room room = roomServiceImpl.createARoom(date,openingHours,closingHours,type);
            roomRepository.save(room);
           return room;
        }else{
            ConflictApiRequestException.throwConflictApiRequestException("Room id:" + found.getId() + " already exists.");
        }
        return null;
    }
    private Room findRoomByRoomTypeAndDate(YogaRooms type,LocalDate date){
        return roomRepository.findRoomByDateAndRoomType(date,type);
    }

    public Room findRoomById(int id) {
        Optional<Room> room = roomRepository.findById(id);
        return room.orElseThrow(()-> NotFoundApiRequestException.throwNotFoundException("Room with id:" + id + " doesn't exist."));
    }



    @Transactional
    public Room addSessionToRoom(Room room, YogaSession session){
        YogaSession foundSession = yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoom(session.getDate(),session.getStartOfSession(),session.getRoom());
        Room foundRoom = roomRepository.findRoomByDateAndRoomType(room.getDate(),room.getRoomType());
        if (foundRoom != null && foundSession == null){
            if(roomServiceImpl.addSessionToRoom(foundRoom,session)){
                roomRepository.save(foundRoom);
                yogaSessionRepository.save(session);
                return foundRoom;
            }
        }
        return null;
    }

    @Transactional
    public boolean removeSessionFromRoom(int roomId, int yogaSessionId) throws ApiRequestException{
        Optional<YogaSession> foundSession = yogaSessionRepository.findById(yogaSessionId);
        YogaSession session = foundSession.orElseThrow(()-> NotFoundApiRequestException.throwNotFoundException("Yoga session with id:" + yogaSessionId + " doesn't exist."));

        Optional<Room> foundRoom = roomRepository.findById(roomId);
        Room room = foundRoom.orElseThrow(()-> NotFoundApiRequestException.throwNotFoundException("Room with id: " + roomId + " doesn't exist."));

        if (roomServiceImpl.removeSessionFromRoom(room, session)){
            roomRepository.save(room);
            yogaSessionRepository.save(session);
            return true;
        }
        NotFoundApiRequestException.throwNotFoundException("Room id:" + room.getId() + " doesn't contain yoga session id:" + session.getId());
        return false;
    }


    public List<YogaSession> getSingleRoomSessionsInADay(YogaRooms type, LocalDate date){
        Room room = findRoomByRoomTypeAndDate(type,date);
        if (room != null){
            return roomServiceImpl.getSingleRoomSessionsInADay(room);
        }
        return null;
    }
    public List<YogaSession> getAllRoomsSessionsInADay(LocalDate date) {
        List<Room> rooms = roomRepository.findAllRoomsByDate(date);
        if (rooms != null && !rooms.isEmpty()){
            return roomServiceImpl.getAllRoomsSessionsInADay(rooms);
        }
        return null;
    }

    public Room findRoomByTypeAndDate(String datePassed, String roomTypePassed) throws ApiRequestException {
        LocalDate date = roomServiceImpl.checkDateFormat(datePassed);
        YogaRooms type = roomServiceImpl.checkRoomTypeFormat(roomTypePassed);
        Room room = roomRepository.findRoomByDateAndRoomType(date,type);
        if (room == null){
            NotFoundApiRequestException.throwNotFoundException("Room on date:" + date + " ,of type:" + YogaRooms.AIR_ROOM.name() +" not found.");
        }
        return room;
    }
}
