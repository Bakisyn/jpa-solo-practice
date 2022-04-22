package dev.milan.jpasolopractice.person.util;

public interface CapitalizePersonName {
     default String capitalizeFirstLetters(String name){
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
