package dev.milan.jpasolopractice.service;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.data.PersonRepository;
import dev.milan.jpasolopractice.data.YogaSessionRepository;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.RoomType;
import dev.milan.jpasolopractice.model.YogaSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;


@Service
public class YogaSessionService {

    private final YogaSessionServiceImpl sessionServiceImpl;
    private final YogaSessionRepository yogaSessionRepository;
    private final  PersonRepository personRepository;
    private final FormatCheckService formatCheckService;
        @Autowired
        public YogaSessionService(YogaSessionServiceImpl sessionServiceImpl, YogaSessionRepository yogaSessionRepository, PersonRepository personRepository
                                    , FormatCheckService formatCheckService) {
        this.sessionServiceImpl = sessionServiceImpl;
        this.yogaSessionRepository = yogaSessionRepository;
        this.personRepository = personRepository;
        this.formatCheckService = formatCheckService;
    }


    @Transactional
    public YogaSession createAYogaSession(String dateString, String roomTypeString, String startTimeString, String durationString) throws ApiRequestException {
        if (dateString == null || roomTypeString == null || startTimeString == null || durationString == null){
            BadRequestApiRequestException.throwBadRequestException("Date, room type, start time and duration must have values assigned.");
            }
        LocalDate date = formatCheckService.checkDateFormat(dateString);
        RoomType roomType = formatCheckService.checkRoomTypeFormat(roomTypeString);
        LocalTime startTime = formatCheckService.checkTimeFormat(startTimeString);
        int duration = formatCheckService.checkNumberFormat(durationString);

        YogaSession found = yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoomType(date,startTime,roomType);
        if (found == null){
            found = sessionServiceImpl.createAYogaSession(date,roomType,startTime,duration);
            yogaSessionRepository.save(found);
        }else{
            ConflictApiRequestException.throwConflictApiRequestException("Yoga session with same date,start time and room type already exists.");
        }
        return found;
    }

    @Transactional
    public boolean addMemberToYogaSession(Person person, YogaSession session){
            YogaSession foundSession = yogaSessionRepository.findYogaSessionByDateAndStartOfSession(session.getDate(),session.getStartOfSession());
            if (foundSession != null){
                Person foundPerson = personRepository.findPersonByEmail(person.getEmail());
                if (foundPerson != null){
                    if(sessionServiceImpl.addMember(foundPerson,foundSession)){
                        foundPerson.addSession(foundSession);
                        personRepository.save(foundPerson);
                        yogaSessionRepository.save(foundSession);
                        return true;
                    }
                }
            }
        return false;
    }
    @Transactional
    public boolean removeMemberFromYogaSession(int sessionId, int personId){
        YogaSession foundSession = yogaSessionRepository.findById(sessionId).orElseThrow(()-> new NotFoundApiRequestException("Yoga session id:" + sessionId +  " couldn't be found."));
        Person foundPerson = personRepository.findById(personId).orElseThrow(() -> new NotFoundApiRequestException("Person id:" + personId + " couldn't be found."));

                if(sessionServiceImpl.removeMember(foundPerson,foundSession)){
                yogaSessionRepository.save(foundSession);
                personRepository.save(foundPerson);
                return true;
                }
                return false;
    }

    public LocalTime getEndOfSession(YogaSession session) {
       return sessionServiceImpl.getEndOfSession(session);
    }

    public int getFreeSpace(YogaSession session) {
        Optional<YogaSession> found = Optional.ofNullable(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoomType(session.getDate(), session.getStartOfSession(), session.getRoomType()));
        return found.map(sessionServiceImpl::getFreeSpace).orElse(-1);
    }

    public YogaSession findYogaSessionById(int yogaSessionId) {
        return yogaSessionRepository.findById(yogaSessionId).orElseThrow(()-> NotFoundApiRequestException.throwNotFoundException("Yoga session id:" + yogaSessionId +  " not found."));
        }

    public List<YogaSession> findAllSessions() {
            return (List<YogaSession>) yogaSessionRepository.findAll();
    }

}
