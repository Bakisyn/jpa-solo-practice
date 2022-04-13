package dev.milan.jpasolopractice.service;

import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.model.RoomType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class FormatCheckService {

    public LocalDate checkDateFormat(String dateToSave) {
        try{
            return LocalDate.parse(dateToSave);
        }catch (Exception e){
            BadRequestApiRequestException.throwBadRequestException("Incorrect date. Correct format is: yyyy-mm-dd");
        }
        return null;
    }

    public LocalTime checkTimeFormat(String timeString) {
        try{
            return LocalTime.parse(timeString);
        }catch (Exception e){
            BadRequestApiRequestException.throwBadRequestException("Incorrect openingHours or closingHours. Acceptable values range from: 00:00:00 to 23:59:59");
        }
        return null;
    }

    public RoomType checkRoomTypeFormat(String yogaRoomType) {
        try{
            return RoomType.valueOf(yogaRoomType.toUpperCase());
        }catch (Exception e){
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
    public Integer checkNumberFormat(String number){
        try{
            return Integer.parseInt(number);
        }catch (Exception e){
            BadRequestApiRequestException.throwBadRequestException("Duration must be an integer value.");
        }
        return null;
    }
}
