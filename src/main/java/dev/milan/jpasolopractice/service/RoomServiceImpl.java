package dev.milan.jpasolopractice.service;

import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.YogaRooms;
import dev.milan.jpasolopractice.model.YogaSession;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
public class RoomServiceImpl {

    public boolean addSession(Room room, YogaSession session){
        long maxDuration = MINUTES.between(room.getOpeningHours(),room.getClosingHours());
        return addSessionIfPossible(room,session,maxDuration);
    }

    private boolean addSessionIfPossible(Room room, YogaSession session, long maxDuration){
        if (session.getRoom() != null && (session.getRoom() == room)){
            long sum = 0;
            if(session.getDate().isEqual(room.getDate())){
                for (YogaSession ses: room.getSessionList()){
                    sum += ses.getDuration();
                }
                if (session.getDuration() <= (maxDuration - sum)){
                    if (room.getSessionList().isEmpty()){
                        room.addSession(session);
                    }else{
                        YogaSession lastSession = room.getSessionList().get(room.getSessionList().size()-1);
                        if (session.getStartOfSession().isBefore(lastSession.getEndOfSession())){
                            return false;
                        }
                        room.addSession(session);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public Room createARoom(LocalDate date, LocalTime openingHours, LocalTime closingHours, YogaRooms type){
        Room room = new Room();
        setDate(room,date);
        setOpeningHours(room,openingHours);
        setClosingHours(room,closingHours);
        room.setRoomType(type);
        room.setTotalCapacity(type.getMaxCapacity());
        return room;
    }

    private void setDate(Room room, LocalDate newDate) {
        if (room.getDate().isBefore(newDate)){
            room.setDate(newDate);
        }else{
            room.setDate(LocalDate.now());
        }
    }


    private void setOpeningHours(Room room, LocalTime openingHours) {
        LocalTime minHours = LocalTime.of(6,0,0);
        if (openingHours.isBefore(minHours) || openingHours.equals(room.getClosingHours()) || openingHours.isAfter(room.getClosingHours())){
            if (room.getClosingHours().equals(openingHours)){
                room.setClosingHours(LocalTime.of(22,0,0));
            }
            room.setOpeningHours(LocalTime.of(8, 0, 0));
        }else{
            room.setOpeningHours(openingHours);
        }
    }


    private void setClosingHours(Room room, LocalTime closingHours) {
        LocalTime maxHours = LocalTime.of(22,0,0);
        if (closingHours.isAfter(maxHours) || closingHours.equals(room.getOpeningHours()) || closingHours.isBefore(room.getOpeningHours())){
            room.setOpeningHours(LocalTime.of(8,0,0));
            room.setClosingHours(maxHours);
        }else{
            room.setClosingHours(closingHours);
        }
    }


    public List<YogaSession> getSingleRoomSessionsInADay(Room room) {
        ArrayList<YogaSession> listOfSessions = new ArrayList<>();
        for(YogaSession session: room.getSessionList()){
            listOfSessions.add((YogaSession) session.clone());
        }
        return  listOfSessions;
    }
    public List<YogaSession> getAllRoomsSessionsInADay(List<Room> rooms) {
        ArrayList<YogaSession> listOfSessions = new ArrayList<>();
        for (Room room : rooms){
            for(YogaSession session: room.getSessionList()){
                listOfSessions.add((YogaSession) session.clone());
            }
        }
        return  listOfSessions;
    }
}
