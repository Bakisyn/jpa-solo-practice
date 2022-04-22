package dev.milan.jpasolopractice.person;

import dev.milan.jpasolopractice.person.Person;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PersonRepository extends CrudRepository<Person, Integer> {

    @Query("select s from Person s where s.email = ?1")
    Person findPersonByEmail(String email);

    List<Person> findPeopleByAgeBetween(int startAge, int endAge);
}
