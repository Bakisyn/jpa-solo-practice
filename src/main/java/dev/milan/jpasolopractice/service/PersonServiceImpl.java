package dev.milan.jpasolopractice.service;

import dev.milan.jpasolopractice.model.Person;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PersonServiceImpl {
    public Person createPerson(String name, int age,String email){
        if(!checkString(name)){
            return null;
        }
        if(!checkAge(age)){
            return null;
        }
        if (!checkEmailFormat(email)){
            return null;
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
        return (age > 9 && age < 81);
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

}
