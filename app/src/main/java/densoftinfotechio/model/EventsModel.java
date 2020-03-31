package densoftinfotechio.model;

import androidx.annotation.NonNull;

public class EventsModel {

    private int DoctorId = 0;
    private String EventDate = "";
    private int EventId = 0;
    private String EventName = "";
    private String FromTime = "";
    private long TotalTime = 0;
    private long ExpectedAudience = 0;

    public EventsModel(){

    }

    public int getDoctorId() {
        return DoctorId;
    }

    public void setDoctorId(int doctorId) {
        DoctorId = doctorId;
    }

    public String getEventDate() {
        return EventDate;
    }

    public void setEventDate(String eventDate) {
        EventDate = eventDate;
    }

    public int getEventId() {
        return EventId;
    }

    public void setEventId(int eventId) {
        EventId = eventId;
    }

    public String getEventName() {
        return EventName;
    }

    public void setEventName(String eventName) {
        EventName = eventName;
    }

    public String getFromTime() {
        return FromTime;
    }

    public void setFromTime(String fromTime) {
        FromTime = fromTime;
    }

    public long getTotalTime() {
        return TotalTime;
    }

    public void setTotalTime(long totalTime) {
        TotalTime = totalTime;
    }

    public long getExpectedAudience() {
        return ExpectedAudience;
    }

    public void setExpectedAudience(long expectedAudience) {
        ExpectedAudience = expectedAudience;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}
