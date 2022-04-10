package dev.milan.jpasolopractice.service;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.SessionNotAvailableException;
import dev.milan.jpasolopractice.data.PersonRepository;
import dev.milan.jpasolopractice.data.YogaSessionRepository;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.YogaSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;


@Service
public class YogaSessionService {

    private final YogaSessionServiceImpl sessionServiceImpl;
    private final YogaSessionRepository yogaSessionRepository;
    private final  PersonRepository personRepository;
        @Autowired
        public YogaSessionService(YogaSessionServiceImpl sessionServiceImpl, YogaSessionRepository yogaSessionRepository, PersonRepository personRepository) {
        this.sessionServiceImpl = sessionServiceImpl;
        this.yogaSessionRepository = yogaSessionRepository;
        this.personRepository = personRepository;
    }


    @Transactional
    public YogaSession createAYogaSession(LocalDate date, Room room, LocalTime startTime, int duration) throws ApiRequestException {
        if (date == null || room == null){
            throw new ApiRequestException("Date and room must not be null./400");
        }
            YogaSession found = yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoom(date,startTime,room);
        if (found == null){
            found =  sessionServiceImpl.createAYogaSession(date,room,startTime,duration);
            if (found != null){
                yogaSessionRepository.save(found);
            }
        }else{
            throw new ApiRequestException("Yoga session with same date,start time and room already exists./409");
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
    public boolean removeMemberFromYogaSession(Person person, YogaSession session){
            Optional<Person> foundPerson = personRepository.findById(person.getId());
            Optional<YogaSession> foundSession = yogaSessionRepository.findById(session.getId());
            if (foundPerson.isPresent() && foundSession.isPresent()) {
                if(sessionServiceImpl.removeMember(foundPerson.get(),foundSession.get())){
                yogaSessionRepository.save(foundSession.get());
                personRepository.save(foundPerson.get());
                return true;
                }
            }
            return false;
    }

    public LocalTime getEndOfSession(YogaSession session) {
       return sessionServiceImpl.getEndOfSession(session);
    }

    public int getFreeSpace(YogaSession session) {
        Optional<YogaSession> found = Optional.ofNullable(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoom(session.getDate(), session.getStartOfSession(), session.getRoom()));
        return found.map(yogaSession -> sessionServiceImpl.getFreeSpace(yogaSession)).orElse(-1);
    }

    public YogaSession findYogaSessionById(int yogaSessionId) {
            return yogaSessionRepository.findById(yogaSessionId).orElseThrow(()-> new ApiRequestException("Yoga session with that id couldn't be found./404"));
    }
}
