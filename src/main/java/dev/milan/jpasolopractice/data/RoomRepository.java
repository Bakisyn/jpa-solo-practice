package dev.milan.jpasolopractice.data;

import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.RoomType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends CrudRepository<Room, Integer> {

    @Query("select e from Room e where e.date = ?1")
    List<Room> findAllRoomsByDate(LocalDate date);

    @Query("select e from Room e where e.date = ?1 and e.roomType = ?2")
    Room findRoomByDateAndRoomType(LocalDate date, RoomType type);
}
