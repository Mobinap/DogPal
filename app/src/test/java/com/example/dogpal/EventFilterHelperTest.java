package com.example.dogpal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.dogpal.models.JoinedEventWrapper;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import com.example.dogpal.models.Event;
import com.example.dogpal.models.JoinedEventWrapper;


public class EventFilterHelperTest {

    private Date now;

    @Before
    public void setUp() {
        now = new GregorianCalendar(2025, Calendar.JULY, 5, 12, 0).getTime(); // Simulate "now"
    }

    private Event createEvent(String date, String time, String status) {
        Event e = new Event();
        e.setEventDate(date);
        e.setEventTime(time);
        e.setStatus(status);
        return e;
    }

    // TC06-01
    @Test
    public void testIsUpcomingEvent() {
        Event event = createEvent("10/7/2025", "14:00", "Active");
        assertTrue(EventFilterHelper.isUpcoming(event, "Attending", now));
    }

    // TC06-02
    @Test
    public void testIsPassedEvent() {
        Event event = createEvent("1/7/2025", "10:00", "Active");
        assertTrue(EventFilterHelper.isPassed(event, "Attending", now));
    }

    // TC06-03
    @Test
    public void testCancelledByOrganizer() {
        Event event = createEvent("10/7/2025", "14:00", "Cancelled");
        assertTrue(EventFilterHelper.isCancelled(event, "Attending"));
    }

    // TC06-04
    @Test
    public void testCancelledByUser() {
        Event event = createEvent("10/7/2025", "14:00", "Active");
        assertTrue(EventFilterHelper.isCancelled(event, "Cancelled"));
    }

    // TC06-05
    @Test
    public void testNoEventsShowEmptyState() {
        List<JoinedEventWrapper> joined = new ArrayList<>();
        List<JoinedEventWrapper> upcoming = new ArrayList<>();
        for (JoinedEventWrapper wrapper : joined) {
            if (EventFilterHelper.isUpcoming(wrapper.getEvent(), wrapper.getParticipationStatus(), now)) {
                upcoming.add(wrapper);
            }
        }
        assertTrue(upcoming.isEmpty());  // No events = empty state
    }

    // TC06-06
    @Test
    public void testMixedEventsFilterUpcomingOnly() {
        List<JoinedEventWrapper> all = new ArrayList<>();
        all.add(new JoinedEventWrapper(createEvent("10/7/2025", "14:00", "Active"), "Attending")); // upcoming
        all.add(new JoinedEventWrapper(createEvent("1/7/2025", "09:00", "Active"), "Attending"));  // passed
        all.add(new JoinedEventWrapper(createEvent("12/7/2025", "14:00", "Cancelled"), "Attending")); // cancelled
        all.add(new JoinedEventWrapper(createEvent("15/7/2025", "14:00", "Active"), "Cancelled")); // cancelled by user

        List<JoinedEventWrapper> upcoming = new ArrayList<>();
        for (JoinedEventWrapper wrapper : all) {
            if (EventFilterHelper.isUpcoming(wrapper.getEvent(), wrapper.getParticipationStatus(), now)) {
                upcoming.add(wrapper);
            }
        }

        assertEquals(1, upcoming.size());
        assertEquals("10/7/2025", upcoming.get(0).getEvent().getEventDate());
    }
}
