package dev.milan.jpasolopractice.yogasession.util;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.roomtype.RoomType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class SessionInputFormatCheckImplTest {
        @Autowired
        private SessionInputFormatCheckImpl sessionInputFormatCheckImpl;

        @Test
        void should_returnLocalDate_when_dateFormatCorrect(){
            LocalDate date = LocalDate.now().plusDays(2);
            String dateString = date.toString();
            assertEquals(date, sessionInputFormatCheckImpl.checkDateFormat(dateString));
        }

        @Test
        void should_throwException400BadRequestWithMessage_when_dateFormatIncorrect(){
            Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> sessionInputFormatCheckImpl.checkDateFormat("12-2022-1"));
            assertEquals("Incorrect date. Correct format is: yyyy-mm-dd", exception.getMessage());
        }

        @Test
        void should_throwException400BadRequestWithMessage_when_timeFormatIncorrect(){
            Exception exception = assertThrows(ApiRequestException.class, ()-> sessionInputFormatCheckImpl.checkTimeFormat("25:01:10"));
            assertEquals("Incorrect openingHours or closingHours. Acceptable values range from: 00:00:00 to 23:59:59",exception.getMessage());
        }
        @Test
        void should_returnLocalTime_when_timeFormatCorrect(){
            LocalTime time = LocalTime.now().plusHours(1);
            String timeString = time.toString();
            assertEquals(time, sessionInputFormatCheckImpl.checkTimeFormat(timeString));
        }
        @Test
        void should_returnYogaRoomType_when_yogaRoomTypeFormatCorrect(){
            String type = RoomType.values()[0].name();
            RoomType roomType = RoomType.valueOf(type);
            assertEquals(roomType, sessionInputFormatCheckImpl.checkRoomTypeFormat(type));
        }

        @Test
        void should_throwException400BadRequestWithMessage_when_yogaRoomTypeFormatIncorrect(){
            Exception exception = assertThrows(ApiRequestException.class, ()-> sessionInputFormatCheckImpl.checkRoomTypeFormat("zzir"));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i< RoomType.values().length; i++){
                sb.append(" ").append(RoomType.values()[i].name());
                if (i < RoomType.values().length-1){
                    sb.append(",");
                }
            }
            assertEquals("Incorrect type. Correct options are:" + sb,exception.getMessage());
        }

    @Test
    void should_throwException400BadRequest_when_numberFormatIncorrect(){
            Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> sessionInputFormatCheckImpl.checkNumberFormat("21s"));
            assertEquals("Number must be an integer value.",exception.getMessage());
    }
    @Test
    void should_returnNumber_when_numberFormatCorrect(){
        assertEquals(21, sessionInputFormatCheckImpl.checkNumberFormat("21"));
    }

}
