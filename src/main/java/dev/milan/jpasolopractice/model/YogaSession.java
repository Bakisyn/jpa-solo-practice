package dev.milan.jpasolopractice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "YOGA_SESSIONS")
public class YogaSession {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY , cascade = CascadeType.PERSIST)
    @JsonIgnoreProperties({"hibernateLazyInitializer","sessionList"})
    private Room room;
    @Column(name = "DATE")
    private LocalDate date;
    @Column(name = "START_TIME")
    private LocalTime startOfSession;
    @Column(name = "DURATION")
    private int duration = 0;
    @Column(name = "END_TIME")
    private LocalTime endOfSession;
    @Column(name = "BOOKED")
    private int bookedSpace = 0;
    @Column(name = "FREE_SPACE")
    private int freeSpace = 0;
    @Enumerated(EnumType.STRING)
    private RoomType roomType;
    @ManyToMany
    @JoinTable(name = "USERS_SESSIONS", joinColumns = @JoinColumn(name = "SESSION_ID"),
            inverseJoinColumns = @JoinColumn(name="USER_ID"))
    private List<Person> membersAttending = new ArrayList<>();

    public List<Person> getMembersAttending() {
        return membersAttending;
    }

    public void setMembersAttending(List<Person> membersAttending) {
        this.membersAttending = membersAttending;
    }

    public void addMember(Person person){
        this.membersAttending.add(person);
    }
    public void removeMember(Person person){
        this.membersAttending.remove(person);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }


    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartOfSession() {
        return startOfSession;
    }

    public void setStartOfSession(LocalTime startOfSession) {
        this.startOfSession = startOfSession;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public LocalTime getEndOfSession() {
        return endOfSession;
    }

    public void setEndOfSession(LocalTime endOfSession) {
        this.endOfSession = endOfSession;
    }

    public int getBookedSpace() {
        return bookedSpace;
    }

    public int getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(int freeSpace) {
        this.freeSpace = freeSpace;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public void setBookedSpace(int bookedSpace) {
        this.bookedSpace = bookedSpace;
    }


    @Override
    public String toString() {
        return "YogaSession{" +
                "id=" + id +
                ", room=" + room +
                ", date=" + date +
                ", startOfSession=" + startOfSession +
                ", duration=" + duration +
                ", endOfSession=" + endOfSession +
                ", bookedSpace=" + bookedSpace +
                ", freeSpace=" + freeSpace +
                '}';
    }

    public void bookOneSpace() {
        this.bookedSpace++;
        this.freeSpace--;
    }
    public void removeOneBooked(){
        this.bookedSpace--;
        this.freeSpace++;
    }

    @Override
    public Object clone() {
        try {
            return  super.clone();
        } catch (CloneNotSupportedException e) {
            YogaSession session = new YogaSession();
            session.setRoom((Room) this.room.clone());
            session.setStartOfSession(this.startOfSession);
            session.setEndOfSession(this.endOfSession);
            session.setDate(this.date);
            session.setDuration(this.duration);
            session.setFreeSpace(this.freeSpace);

            ArrayList<Person> list = new ArrayList<>();
            for (Person person : membersAttending){
                list.add((Person) person.clone());
            }
            session.setMembersAttending(list);

            return session;
        }
    }

@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()){
        return false;
    }
    YogaSession session = (YogaSession) o;
    if (!Objects.equals(id , session.getId())){
        return false;
    }

    if (!Objects.equals(date, session.getDate())){
        return false;
    }if (!Objects.equals(startOfSession, session.getStartOfSession())){
        return false;
    }
    if (!Objects.equals(roomType, session.getRoomType())){
        return false;
    }
    if (!Objects.equals(room, session.getRoom())){
        return false;
    }
    return  true;
}

    @Override
    public int hashCode() {
        return Objects.hash(id,date,startOfSession,roomType,room);
    }
}
