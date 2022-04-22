package dev.milan.jpasolopractice.yogasession;

import dev.milan.jpasolopractice.roomtype.RoomType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface YogaSessionRepository extends CrudRepository<YogaSession,Integer> {
    @Query("select e from YogaSession e where e.date=?1 and e.startOfSession=?2")
    YogaSession findYogaSessionByDateAndStartOfSession(LocalDate date, LocalTime startOfSession);

    @Query("select e from YogaSession e where e.date=?1 and e.startOfSession=?2 and e.roomType=?3")
    YogaSession findYogaSessionByDateAndStartOfSessionAndRoomType (LocalDate date, LocalTime startOfSession, RoomType roomType);

    List<YogaSession> findYogaSessionByRoomIsNotNull();

    List<YogaSession> findYogaSessionByDateAndRoomIsNotNull(LocalDate date);

    List<YogaSession> findYogaSessionByRoomTypeAndRoomIsNotNull(RoomType type);

    List<YogaSession> findYogaSessionByRoomTypeAndDateAndRoomIsNotNull(RoomType type, LocalDate date);

    List<YogaSession> findYogaSessionByDateAndRoomIsNull(LocalDate date);

    List<YogaSession> findYogaSessionByRoomIsNull();


}
