package dev.milan.jpasolopractice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "PERSON_DATA")
public class Person implements Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "age", nullable = false)
    private int age;
    @Column(name = "EMAIL" ,nullable = false, unique = true)
    private String email;
    @ManyToMany(mappedBy = "membersAttending", fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = "membersAttending")
    private List<YogaSession> yogaSessions = new ArrayList<>();


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<YogaSession> getYogaSessions() {
        return yogaSessions;
    }

    public void setYogaSessions(List<YogaSession> yogaSessions) {
        this.yogaSessions = yogaSessions;
    }
    public void addSession(YogaSession session){
        this.yogaSessions.add(session);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", email='" + email + '\'' +
                ", yogaSessions=" + yogaSessions +
                '}';
    }

    @Override
    public Object clone() {
        try {
            return  super.clone();
        } catch (CloneNotSupportedException e) {
            Person person = new Person();
            person.setName(this.name);
            person.setAge(this.age);
            person.setEmail(this.email);
            List<YogaSession> temp = new ArrayList<>();
            for (YogaSession s : yogaSessions){
                temp.add((YogaSession) s.clone());
            }
            person.setYogaSessions(temp);
            return person;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        Person person = (Person) o;
        if (!Objects.equals(id , person.getId())){
            return false;
        }
        if (!Objects.equals(age ,person.getAge())){
            return false;
        }
        if (!Objects.equals(name, person.getName())){
            return false;
        }if (!Objects.equals(email,person.getEmail())){
            return false;
        }if (!Objects.equals(yogaSessions,person.getYogaSessions())){
            return false;
        }
            return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,age,name,email,yogaSessions);
    }
}
