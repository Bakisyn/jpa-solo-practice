package dev.milan.jpasolopractice.person.util;

import dev.milan.jpasolopractice.person.Person;

public interface PersonCreator {
    Person createPerson(String name, int age, String email);
}
