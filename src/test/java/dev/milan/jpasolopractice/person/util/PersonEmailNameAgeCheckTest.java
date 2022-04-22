package dev.milan.jpasolopractice.person.util;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
@SpringBootTest
public class PersonEmailNameAgeCheckTest {

    @Autowired
    private PersonEmailNameAgeCheck personEmailNameAgeCheck;

    @Nested
    class CreatePersonTest {
        @Test
        public void should_failToCreatePerson_when_creatingAPerson_and_nameIsNotAllCharacters(){
            String name = "sok&od";
            Exception exception = assertThrows(ApiRequestException.class, () -> personEmailNameAgeCheck.checkPersonData(name,22,"alias@yahoo.com"));
            assertEquals("Bad name formatting. Name must only contain alphabetical characters and be below 100 characters in length.",exception.getMessage());
        }
        @Test
        public void should_failToCreatePerson_when_creatingAPerson_and_nameLongerThan100Characters(){
            String name = "wdkaodoadoakdoaodsodoasdoaodoaodosoaowdoosoaowdoosoaowdkokaodkoakdoakdoakdoskoadkosakdsoadoaskdosakdokadokasodkasodkaodkaokdaowkoadoawodoaeaepldsalpdalwdoskaodkaokda";
            Exception exception = assertThrows(ApiRequestException.class, () -> personEmailNameAgeCheck.checkPersonData(name,20,"alias@yahoo.com"));
            assertEquals("Bad name formatting. Name must only contain alphabetical characters and be below 100 characters in length.",exception.getMessage());
        }

        @Test
        public void should_failToCreatePerson_when_creatingAPerson_and_ageBelowMinimumAge(){
            int minAge = personEmailNameAgeCheck.getMIN_AGE();
            int maxAge = personEmailNameAgeCheck.getMAX_AGE();
            Exception exception = assertThrows(ApiRequestException.class, () -> personEmailNameAgeCheck.checkPersonData("Arthur Shopenhauer",minAge-1,"alias@yahoo.com"));
            assertEquals("Age must be between " + minAge + " and " + maxAge + "." , exception.getMessage());
        }
        @Test
        public void should_failToCreatePerson_when_creatingAPerson_and_ageAboveMaxAge(){
            int minAge = personEmailNameAgeCheck.getMIN_AGE();
            int maxAge = personEmailNameAgeCheck.getMAX_AGE();
            Exception exception = assertThrows(ApiRequestException.class, () -> personEmailNameAgeCheck.checkPersonData("Arthur Shopenhauer",maxAge+1,"alias@yahoo.com"));
            assertEquals("Age must be between " + minAge + " and " + maxAge + "." , exception.getMessage());
        }
        @Test
        public void should_failToCreatePerson_when_creatingAPerson_and_emailIsWrongFormat(){
            String email = "wjijdawd.c";
            Exception exception = assertThrows(ApiRequestException.class, () -> personEmailNameAgeCheck.checkPersonData("Arthur Shopenhauer",39,email));
            assertEquals("Incorrect email format. Email must only contain alphabetical characters, numbers, and one @ and end with .com or .org or .net.", exception.getMessage());
        }
    }
}
