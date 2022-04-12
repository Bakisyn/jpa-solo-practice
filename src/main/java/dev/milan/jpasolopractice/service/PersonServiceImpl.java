package dev.milan.jpasolopractice.service;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.model.Person;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PersonServiceImpl {
    private final int MIN_AGE = 9;
    private final int MAX_AGE = 81;
    public Person createPerson(String name, int age,String email) throws ApiRequestException{
        if(!checkString(name)){
            BadRequestApiRequestException.throwBadRequestException("Bad name formatting. Name must only contain alphabetical characters and be below 100 characters in length.");
        }
        if(!checkAge(age)){
            BadRequestApiRequestException.throwBadRequestException("Age must be between " + MIN_AGE + " and " + MAX_AGE + ".");
        }
        if (!checkEmailFormat(email)){
            BadRequestApiRequestException.throwBadRequestException("Incorrect email format. Email must only contain alphabetical characters, numbers, and one @ and end with .com or .org or .net.");
        }
        Person person = new Person();
        person.setName(capitalizeFirstLetters(name));
        person.setAge(age);
        person.setEmail(email);
        return person;
    }
    private boolean checkEmailFormat(String email){
        Pattern pattern = Pattern.compile("[a-zA-Z0-9]+(@){1}[a-zA-Z]+(.){1}(com|org|net)");
        Matcher  matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean checkString(String name){
        if (name.length() > 100){
            return false;
        }
        boolean isLetter = true;
        if (name.contains(" ")){
            String[] data = name.split(" ");
            for (String word : data) {
                isLetter = checkIfCharsAreLetters(word);
                if (!isLetter) {
                    break;
                }
            }
        }else{
            isLetter = checkIfCharsAreLetters(name);
        }
        return isLetter;
    }

    private boolean checkIfCharsAreLetters(String word){
        for (int j=0; j<word.length(); j++){
            if (!Character.isLetter(word.charAt(j))){
                return false;
            }
        }
        return true;
    }

    private boolean checkAge(int age){
        return (age > MIN_AGE && age < MAX_AGE);
    }

    private String capitalizeFirstLetters(String name){
        String[] data = name.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<data.length; i++){
            sb.append(data[i].substring(0,1).toUpperCase());
            sb.append(data[i].substring(1));
            if (i != data.length-1){
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public int getMIN_AGE() {
        return MIN_AGE;
    }

    public int getMAX_AGE() {
        return MAX_AGE;
    }
}
