package dev.milan.jpasolopractice.service;

import dev.milan.jpasolopractice.customException.ApiRequestException;
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
    private final LocalTime MIN_OPENING_HOURS = LocalTime.of(6,0,0);
    private final LocalTime MAX_CLOSING_HOURS = LocalTime.of(23,0,0);

    public boolean addSessionToRoom(Room room, YogaSession session){
        long maxDuration = MINUTES.between(room.getOpeningHours(),room.getClosingHours());
        return addSessionToRoomIfPossible(room,session,maxDuration);
    }

    private boolean addSessionToRoomIfPossible(Room room, YogaSession session, long maxDuration){
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
        if (openingHours.isBefore(MIN_OPENING_HOURS) || openingHours.equals(room.getClosingHours()) || openingHours.isAfter(room.getClosingHours())){
            if (room.getClosingHours().equals(openingHours)){
                room.setClosingHours(MAX_CLOSING_HOURS);
            }
            room.setOpeningHours(MIN_OPENING_HOURS);
        }else{
            room.setOpeningHours(openingHours);
        }
    }


    private void setClosingHours(Room room, LocalTime closingHours) {
        if (closingHours.isAfter(MAX_CLOSING_HOURS) || closingHours.equals(room.getOpeningHours()) || closingHours.isBefore(room.getOpeningHours())){
            room.setOpeningHours(MIN_OPENING_HOURS);
            room.setClosingHours(MAX_CLOSING_HOURS);
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

    public boolean removeSessionFromRoom(Room room, YogaSession session) {
        if (room.getSessionList().contains(session)){
            room.getSessionList().remove(session);
            session.setRoom(null);
            return true;
        }
            return false;
    }

    public LocalTime getMIN_OPENING_HOURS() {
        return MIN_OPENING_HOURS;
    }

    public LocalTime getMAX_CLOSING_HOURS() {
        return MAX_CLOSING_HOURS;
    }

    public LocalDate checkDateFormat(String dateToSave) {
        System.out.println("Before entering try");
        try{
            System.out.println("DATE IN check method " + dateToSave);

            LocalDate date = LocalDate.parse(dateToSave);
            return date;
        }catch (Exception e){
            System.out.println("In c");
            throw new ApiRequestException("Incorrect date. Correct format is: yyyy-mm-dd/400");
        }
    }

    public LocalTime checkTimeFormat(String timeString) {
        try{
            return LocalTime.parse(timeString);
        }catch (Exception e){
            throw new ApiRequestException("Incorrect openingHours or closingHours. Acceptable values range from: 00:00:00 to 23:59:59/400");
        }
    }

    public YogaRooms checkRoomTypeFormat(String yogaRoomType) {
        try{
            return YogaRooms.valueOf(yogaRoomType);
        }catch (Exception e){
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<YogaRooms.values().length; i++){
                sb.append(" " + YogaRooms.values()[i].name());
                if (i < YogaRooms.values().length-1){
                    sb.append(",");
                }
            }
            throw new ApiRequestException("Incorrect type. Correct options are:" + sb + "/400");

        }
    }
}
