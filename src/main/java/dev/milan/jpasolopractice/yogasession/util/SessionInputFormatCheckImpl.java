package dev.milan.jpasolopractice.yogasession.util;

import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.roomtype.RoomType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class SessionInputFormatCheckImpl implements SessionInputChecker {

    @Override
    public LocalDate checkDateFormat(String dateToSave) throws BadRequestApiRequestException{
        try{
            return LocalDate.parse(dateToSave);
        }catch (Exception e){
            BadRequestApiRequestException.throwBadRequestException("Incorrect date. Correct format is: yyyy-mm-dd");
        }
        return null;
    }

    @Override
    public LocalTime checkTimeFormat(String timeString) throws BadRequestApiRequestException{
        try{
            return LocalTime.parse(timeString);
        }catch (Exception e){
            BadRequestApiRequestException.throwBadRequestException("Incorrect openingHours or closingHours. Acceptable values range from: 00:00:00 to 23:59:59");
        }
        return null;
    }

    @Override
    public RoomType checkRoomTypeFormat(String yogaRoomType) throws BadRequestApiRequestException {
        try{
            System.out.println("rOOM TYPE CHECKED IS " + yogaRoomType);
            return RoomType.valueOf(yogaRoomType.toUpperCase());
        }catch (Exception e){
            e.printStackTrace();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i< RoomType.values().length; i++){
                sb.append(" ").append(RoomType.values()[i].name());
                if (i < RoomType.values().length-1){
                    sb.append(",");
                }
            }
            BadRequestApiRequestException.throwBadRequestException("Incorrect type. Correct options are:" + sb);
        }
        return null;
    }
    @Override
    public Integer checkNumberFormat(String number) throws BadRequestApiRequestException{
        try{
            return Integer.parseInt(number);
        }catch (Exception e){
            BadRequestApiRequestException.throwBadRequestException("Number must be an integer value.");
        }
        return null;
    }
}
