package dev.milan.jpasolopractice.service;

import dev.milan.jpasolopractice.customException.SessionNotAvailableException;
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
    public Room createARoom(LocalDate date, LocalTime openingHours, LocalTime closingHours, YogaRooms type) {
        Room found = findRoomByRoomTypeAndDate(type,date);
        if (found == null){
           Room room = roomServiceImpl.createARoom(date,openingHours,closingHours,type);
           roomRepository.save(room);
           return room;
        }
        return null;
    }

    public Room findRoomById(int id){
        Optional<Room> room = roomRepository.findById(id);
        return room.orElse(null);
    }

    private Room findRoomByRoomTypeAndDate(YogaRooms type,LocalDate date){
        return roomRepository.findRoomByNameAndDate(type,date);
    }

    @Transactional
    public Room addSession(Room room, YogaSession session){
        YogaSession foundSession = yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoom(session.getDate(),session.getStartOfSession(),session.getRoom());
        Room foundRoom = roomRepository.findRoomByNameAndDate(room.getRoomType(),room.getDate());
        if (foundRoom != null && foundSession == null){
            if(roomServiceImpl.addSession(foundRoom,session)){
                roomRepository.save(foundRoom);
                yogaSessionRepository.save(session);
                return foundRoom;
            }
        }
        return null;
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
}
