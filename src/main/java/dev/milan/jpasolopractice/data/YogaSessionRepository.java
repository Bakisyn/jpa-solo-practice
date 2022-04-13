package dev.milan.jpasolopractice.data;

import dev.milan.jpasolopractice.model.RoomType;
import dev.milan.jpasolopractice.model.YogaSession;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.time.LocalTime;

public interface YogaSessionRepository extends CrudRepository<YogaSession,Integer> {
    @Query("select e from YogaSession e where e.date=?1 and e.startOfSession=?2")
    YogaSession findYogaSessionByDateAndStartOfSession(LocalDate date, LocalTime startOfSession);

    @Query("select e from YogaSession e where e.date=?1 and e.startOfSession=?2 and e.roomType=?3")
    YogaSession findYogaSessionByDateAndStartOfSessionAndRoomType (LocalDate date, LocalTime startOfSession, RoomType roomType);

}
