package dev.milan.jpasolopractice.data;

import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.YogaRooms;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends CrudRepository<Room, Integer> {
    @Query("select e from Room e where e.roomType=?1 and e.date=?2")
    Room findRoomByNameAndDate(YogaRooms roomType, LocalDate date);

    @Query("select e from Room e where e.date = ?1")
    List<Room> findAllRoomsByDate(LocalDate date);

    @Query("select e from Room e where e.date = ?1 and e.roomType = ?2")
    Room findRoomByDateAndRoomType(LocalDate date, YogaRooms type);
}
