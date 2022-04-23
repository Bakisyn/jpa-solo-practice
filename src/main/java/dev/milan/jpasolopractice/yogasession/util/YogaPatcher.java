package dev.milan.jpasolopractice.yogasession.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ForbiddenApiRequestException;
import dev.milan.jpasolopractice.person.Person;
import dev.milan.jpasolopractice.room.Room;
import dev.milan.jpasolopractice.room.RoomRepository;
import dev.milan.jpasolopractice.room.RoomService;
import dev.milan.jpasolopractice.room.util.RoomUtil;
import dev.milan.jpasolopractice.shared.Patcher;
import dev.milan.jpasolopractice.yogasession.YogaSession;
import dev.milan.jpasolopractice.yogasession.YogaSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component("yogaPatcher")
public class YogaPatcher implements Patcher<YogaSession> {
    private final RoomUtil roomUtil;
    private final RoomService roomService;
    private final YogaSessionRepository yogaSessionRepository;
    private final RoomRepository roomRepository;
    private final YogaSessionUtil yogaSessionUtil;
    private final SessionInputChecker sessionInputChecker;
    private ObjectMapper mapper;
    @Autowired
    public YogaPatcher(RoomUtil roomUtil, RoomService roomService, YogaSessionRepository yogaSessionRepository, RoomRepository roomRepository, YogaSessionUtil yogaSessionUtil, SessionInputChecker sessionInputChecker, ObjectMapper mapper) {
        this.roomUtil = roomUtil;
        this.roomService = roomService;
        this.yogaSessionRepository = yogaSessionRepository;
        this.roomRepository = roomRepository;
        this.yogaSessionUtil = yogaSessionUtil;
        this.sessionInputChecker = sessionInputChecker;
        this.mapper = mapper;
    }


    public YogaSession patch(JsonPatch patch, YogaSession session) throws ApiRequestException{
        YogaSession patchedSession = applyPatchToSession(patch, session);

        return updateSession(session, patchedSession);
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
            if (roomUtil.canAddSessionToRoom(room, patchedSession)){
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
            Room room = findRoomByDateAndType(patchedSession.getDate().toString(),patchedSession.getRoomType().name());
            patchedSession = setUpASessionForRoomOrDateChange(sessionFound, patchedSession);
            patchedSession.setRoom(null);

            if (roomUtil.canAddSessionToRoom(room, patchedSession)){
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
        patchedSession = yogaSessionUtil.createAYogaSession(patchedSession.getDate(),patchedSession.getRoomType()
                ,patchedSession.getStartOfSession(),patchedSession.getDuration());
        patchedSession.setId(sessionFound.getId());
        patchedSession.setMembersAttending(sessionFound.getMembersAttending());
        return patchedSession;
    }
    private YogaSession changeSessionWithoutARoom(YogaSession session) throws ApiRequestException{
        int id = session.getId();
        List<Person> members = session.getMembersAttending();
        session = yogaSessionUtil.createAYogaSession(sessionInputChecker.checkDateFormat(session.getDate().toString())
                , sessionInputChecker.checkRoomTypeFormat(session.getRoomType().name()), sessionInputChecker.checkTimeFormat(session.getStartOfSession().toString())
                , sessionInputChecker.checkNumberFormat("" + session.getDuration()));
        session.setId(id);
        session.setMembersAttending(members);
        return yogaSessionRepository.save(session);
    }


    private YogaSession applyPatchToSession(JsonPatch patch, YogaSession targetSession) throws BadRequestApiRequestException{
        try{
            JsonNode patched = patch.apply(mapper.convertValue(targetSession, JsonNode.class));
            return mapper.treeToValue(patched, YogaSession.class);
        } catch (JsonPatchException | JsonProcessingException e) {
            BadRequestApiRequestException.throwBadRequestException("Incorrect patch request data.");
        }
        return targetSession;
    }

    private Room findRoomByDateAndType(String date , String roomType)  throws BadRequestApiRequestException{
        return  roomService.findSingleRoomByDateAndType(sessionInputChecker.checkDateFormat(date)
                , sessionInputChecker.checkRoomTypeFormat(roomType));
    }

}
