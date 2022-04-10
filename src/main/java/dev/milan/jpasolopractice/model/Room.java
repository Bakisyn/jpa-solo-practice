package dev.milan.jpasolopractice.model;


import dev.milan.jpasolopractice.service.RoomServiceImpl;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Table(name="ROOMS")
@Entity
public class Room implements Cloneable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(name = "DATE")
    private LocalDate date = LocalDate.now();
    @Column(name = "OPEN")
    private LocalTime openingHours = LocalTime.of(8, 0, 0);
    @Column(name = "CLOSE")
    private LocalTime closingHours = LocalTime.of(22, 0, 0);

    @Enumerated(EnumType.STRING)
    private YogaRooms roomType;
    @Column(name = "CAPACITY")
    private int totalCapacity;
    @OneToMany(mappedBy = "room", fetch = FetchType.EAGER)
    private List<YogaSession> sessionList = new ArrayList<>();

    public void addSession(YogaSession session){
        this.sessionList.add(session);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(LocalTime openingHours) {
        this.openingHours = openingHours;
    }

    public LocalTime getClosingHours() {
        return closingHours;
    }

    public void setClosingHours(LocalTime closingHours) {
        this.closingHours = closingHours;
    }

    public YogaRooms getRoomType() {
        return roomType;
    }

    public void setRoomType(YogaRooms roomType) {
        this.roomType = roomType;
        getTotalCapacity();
    }

    public int getTotalCapacity() {
        if (totalCapacity == 0){
            if (this.roomType != null){
                this.totalCapacity = this.roomType.getMaxCapacity();
            }
        }
        return this.totalCapacity;
    }
    public void setTotalCapacity(int capacity) {
        this.totalCapacity = capacity;
    }

    public List<YogaSession> getSessionList() {
        return sessionList;
    }

    public void setSessionList(List<YogaSession> sessionList) {
        this.sessionList = sessionList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Room{" +
                "date=" + date +
                ", openingHours=" + openingHours +
                ", closingHours=" + closingHours +
                ", roomType=" + roomType +
                ", totalCapacity=" + totalCapacity +
                ", sessionList=" + sessionList +
                '}';
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            Room room = new Room();
            room.setRoomType(this.roomType);
            room.setTotalCapacity(this.getTotalCapacity());
            room.setOpeningHours(this.openingHours);
            room.setClosingHours(this.closingHours);
            room.setDate(this.date);
            ArrayList<YogaSession> list = new ArrayList<>();
            for (YogaSession session : sessionList){
                list.add((YogaSession) session.clone());
            }
            room.setSessionList(list);
            return room;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(id,room.id) && Objects.equals(date,room.date) && Objects.equals(roomType,room.roomType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, roomType);
    }
}
