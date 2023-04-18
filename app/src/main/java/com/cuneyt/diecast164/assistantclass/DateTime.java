package com.cuneyt.diecast164.assistantclass;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateTime {

    public String currentlyDateTime(){
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Calendar calendar = Calendar.getInstance();

        String dt = dateFormat.format(calendar.getTime());

        return dt;
    }

}
