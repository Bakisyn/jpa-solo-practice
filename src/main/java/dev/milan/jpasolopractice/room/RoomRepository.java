package dev.milan.jpasolopractice.room;

import dev.milan.jpasolopractice.roomtype.RoomType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends CrudRepository<Room, Integer> {

    @Query("select e from Room e where e.date = ?1")
    List<Room> findAllRoomsByDate(LocalDate date);

    @Query("select e from Room e where e.date = ?1 and e.roomType = ?2")
    Room findSingleRoomByDateAndType(LocalDate date, RoomType type);

    List<Room> findRoomsByRoomType(RoomType type);

}
