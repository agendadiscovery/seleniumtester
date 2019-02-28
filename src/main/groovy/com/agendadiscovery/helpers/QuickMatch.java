package com.agendadiscovery.helpers;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuickMatch {
    public static String match(String regex, String s){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        if (matcher.find())
        {
            System.out.println(matcher.group(0));
            return matcher.group(0);
        }
        else{
            return null;
        }
    }

    public static String matchGroup(String regex, String s){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        if (matcher.find())
        {
            System.out.println(matcher.group(1));
            return matcher.group(1);
        }
        else{
            return null;
        }
    }

    public static Matcher matchGroups(String regex, String s){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        if (matcher.find())
        {
            return matcher;
        }
        else{
            return null;
        }
    }

}



//module created by Randall Babaoye.  randallcoding@protonmail.com  2/16/19