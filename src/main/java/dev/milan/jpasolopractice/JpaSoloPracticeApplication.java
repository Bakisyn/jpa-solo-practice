package dev.milan.jpasolopractice;

import dev.milan.jpasolopractice.person.PersonRepository;
import dev.milan.jpasolopractice.person.PersonService;
import dev.milan.jpasolopractice.room.RoomService;
import dev.milan.jpasolopractice.yogasession.YogaSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JpaSoloPracticeApplication {
	@Autowired
	private PersonRepository personRepository;
	@Autowired
	private RoomService roomService;
	@Autowired
	private PersonService personService;
	@Autowired
	private YogaSessionService yogaService;

	public static void main(String[] args) {
		SpringApplication.run(JpaSoloPracticeApplication.class, args);
	}

//	@PostConstruct    //ovo sprecava times(1) u RoomServiceTest
//	public void start() throws SessionNotAvailableException {
//		Room roomOne = new Room();
//		roomOne.setRoomType(YogaRooms.AIR_ROOM);
//		Room roomTwo = new Room();
//		roomTwo.setRoomType(YogaRooms.EARTH_ROOM);
//
//		int amount = 31;
//		LocalTime toStart = roomOne.getOpeningHours().plus(amount, MINUTES);
//		YogaSession sessionOne = yogaService.createAYogaSession(LocalDate.now(),roomOne, toStart,90);
//		YogaSession sessionTwo = yogaService.createAYogaSession(LocalDate.now().plus(3,DAYS),roomTwo, LocalTime.of(13,9,8),90);
//
//		roomService.addSession(roomOne,sessionOne);
//		roomService.addSession(roomTwo,sessionTwo);
//
//		Person person1 = personService.addPerson("Marko",22,"bacaw@gmail.com");
//		Person person2 = personService.addPerson("Stevam",24,"baww@gmail.com");
//
//
//
//			yogaService.addMember(person1,sessionOne);
//			System.out.println(" PERSON1 should contain 1 session" + personService.getAllSessionsFromPerson(person1).size());
//			System.out.println(" SESSION ONE should contain 1 person " + (sessionOne.getRoom().getTotalCapacity() - yogaService.getFreeSpace(sessionOne)));
//
//			yogaService.addMember(person1,sessionTwo);
//
//			System.out.println(" PERSON1 should contain 2 session" + personService.getAllSessionsFromPerson(person1).size());
//			System.out.println(" SESSION TWO should contain 1 person " + (sessionTwo.getRoom().getTotalCapacity() - yogaService.getFreeSpace(sessionTwo)));
//			yogaService.addMember(person2,sessionOne);
//
////			yogaService.removeMember(person1,sessionOne);
//
//			System.out.println(" PERSON1 should contain 1 session" + personService.getAllSessionsFromPerson(person1).size());
//			System.out.println(" SESSION ONE should contain 1 person " + (sessionOne.getRoom().getTotalCapacity() - yogaService.getFreeSpace(sessionOne)));
//			System.out.println("Free space in sessionOne is " + yogaService.getFreeSpace(sessionOne));
//
//
//			System.out.println(" PERSON1 should contain 1 session" + personService.getAllSessionsFromPerson(person1).size());
//			System.out.println(" SESSION ONE should contain 1 person " + (sessionOne.getRoom().getTotalCapacity() - yogaService.getFreeSpace(sessionOne)));
//			System.out.println("Free space in sessionOne is " + yogaService.getFreeSpace(sessionOne));
//
//			System.out.println(" PERSON1 should contain 1 session" + personService.getAllSessionsFromPerson(person1).size());
//			System.out.println(" SESSION ONE should contain 1 person " + (sessionOne.getRoom().getTotalCapacity() - yogaService.getFreeSpace(sessionOne)));
//			System.out.println("Free space in sessionOne is " + yogaService.getFreeSpace(sessionOne));
//
//			System.out.println(" PERSON1 should contain 1 session" + personService.getAllSessionsFromPerson(person1).size());
//			System.out.println(" SESSION ONE should contain 1 person " + (sessionOne.getRoom().getTotalCapacity() - yogaService.getFreeSpace(sessionOne)));
//
//			System.out.println("Free space in sessionOne is " + yogaService.getFreeSpace(sessionOne));
//			for (Person p : sessionOne.getMembersAttending()){
//				System.out.println("Person : " + p);
//			}
//		System.out.println(" PERSON1 should contain 2 session" + personService.getAllSessionsFromPerson(person1).size());
//
//
//	}

	//		//dodati u person tabelu yoga session id , mozda many to many da person ima u kojim je sessions







}
