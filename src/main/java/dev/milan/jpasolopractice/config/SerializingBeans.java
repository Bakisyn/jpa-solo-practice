package dev.milan.jpasolopractice.config;

import dev.milan.jpasolopractice.person.Person;
import dev.milan.jpasolopractice.room.Room;
import dev.milan.jpasolopractice.shared.Patcher;
import dev.milan.jpasolopractice.yogasession.YogaSession;
import dev.milan.jpasolopractice.yogasession.util.YogaPatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SerializingBeans {
//    @Bean
//    public Patcher<YogaSession> yogaSessionPatcher(){
//        return new YogaPatcher();
//    }
//    @Bean
//    public Patcher<Person> personPatcher(){
//        return new PersonPatcher();
//    }
//    @Bean
//    public Patcher<Room> roomPatcher(){
//        return new roomPatcher();
//    }
}
