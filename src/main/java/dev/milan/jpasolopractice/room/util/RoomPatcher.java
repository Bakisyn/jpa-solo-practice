package dev.milan.jpasolopractice.room.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.room.Room;
import dev.milan.jpasolopractice.room.RoomRepository;
import dev.milan.jpasolopractice.shared.Patcher;
import dev.milan.jpasolopractice.yogasession.YogaSession;
import dev.milan.jpasolopractice.yogasession.YogaSessionRepository;
import dev.milan.jpasolopractice.yogasession.util.SessionInputChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
@Component
public class RoomPatcher implements Patcher<Room> {
    private final RoomRepository roomRepository;
    private final RoomUtil roomUtil;
    private final YogaSessionRepository yogaSessionRepository;
    private final SessionInputChecker sessionInputChecker;
    private final ObjectMapper mapper;

    @Autowired
    public RoomPatcher(RoomRepository roomRepository, RoomUtil roomUtil, YogaSessionRepository yogaSessionRepository
            , SessionInputChecker sessionInputChecker, ObjectMapper mapper) {
        this.yogaSessionRepository = yogaSessionRepository;
        this.roomRepository = roomRepository;
        this.roomUtil = roomUtil;
        this.sessionInputChecker = sessionInputChecker;
        this.mapper = mapper;
    }
    @Override
    public Room patch(JsonPatch patch, Room room) throws ApiRequestException {
        Room patchedRoom = applyPatchToRoom(patch,room);

        List<YogaSession> sessions = patchedRoom.getSessionList();
        int id = patchedRoom.getId();
        patchedRoom = roomUtil.createARoom(sessionInputChecker.checkDateFormat(patchedRoom.getDate().toString())
                , sessionInputChecker.checkTimeFormat(patchedRoom.getOpeningHours().toString())
                , sessionInputChecker.checkTimeFormat(patchedRoom.getClosingHours().toString())
                , sessionInputChecker.checkRoomTypeFormat(patchedRoom.getRoomType().name()));
        patchedRoom.setSessionList(sessions);
        patchedRoom.setId(id);
        return updateRoom(room,patchedRoom);
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
                return updateRoomDate(patchedRoom, (openingTimeDontMatch || closingTimeDontMatch));
            }if (openingTimeDontMatch || closingTimeDontMatch){
                return updateRoomTime(patchedRoom);
            }
        }
        return null;
    }

    private Room updateRoomTime(Room patchedRoom) {
        List<YogaSession> modifiedSessionList = new ArrayList<>();
        for (YogaSession session: patchedRoom.getSessionList()){
            if (session.getStartOfSession().isBefore(patchedRoom.getOpeningHours()) || session.getEndOfSession().isAfter(patchedRoom.getClosingHours())){
                session.setRoom(null);
                yogaSessionRepository.save(session);
            }else{
                modifiedSessionList.add(session);
            }
        }
        patchedRoom.setSessionList(modifiedSessionList);
        return roomRepository.save(patchedRoom);
    }

    private Room updateRoomDate(Room patchedRoom, boolean timesDontMatch) throws ApiRequestException{
        Room room = roomRepository.findSingleRoomByDateAndType(patchedRoom.getDate(), patchedRoom.getRoomType());
        if (room == null){
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
