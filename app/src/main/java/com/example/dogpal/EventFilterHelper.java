package com.example.dogpal;

import com.example.dogpal.models.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventFilterHelper {

    public static boolean isUpcoming(Event event, String participationStatus, Date now) {
        if (isCancelled(event, participationStatus)) return false;
        Date eventDateTime = getEventDateTime(event);
        return eventDateTime != null && eventDateTime.after(now)
                && "Attending".equalsIgnoreCase(participationStatus);
    }

    public static boolean isCancelled(Event event, String participationStatus) {
        return "Cancelled".equalsIgnoreCase(event.getStatus())
                || "Cancelled".equalsIgnoreCase(participationStatus);
    }

    public static boolean isPassed(Event event, String participationStatus, Date now) {
        if (isCancelled(event, participationStatus)) return false;
        Date eventDateTime = getEventDateTime(event);
        return eventDateTime != null && eventDateTime.before(now)
                && !"Cancelled".equalsIgnoreCase(participationStatus);
    }

    public static Date getEventDateTime(Event event) {
        try {
            String eventDateTimeString = event.getEventDate() + " " + event.getEventTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());
            return dateFormat.parse(eventDateTimeString);
        } catch (Exception e) {
            return null;
        }
    }
}
