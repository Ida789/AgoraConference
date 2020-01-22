package densoftinfotechio.model;

import androidx.annotation.NonNull;

public class EventsModel {

    private String DoctorId = "";
    private String EventDate = "";
    private String EventId = "";
    private String EventName = "";
    private String FromTime = "";
    private String TotalTime = "";
    private String ExpectedAudience = "";

    public EventsModel(){

    }

    public String getDoctorId() {
        return DoctorId;
    }

    public void setDoctorId(String doctorId) {
        DoctorId = doctorId;
    }

    public String getEventDate() {
        return EventDate;
    }

    public void setEventDate(String eventDate) {
        EventDate = eventDate;
    }

    public String getEventId() {
        return EventId;
    }

    public void setEventId(String eventId) {
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

    public String getTotalTime() {
        return TotalTime;
    }

    public void setTotalTime(String totalTime) {
        TotalTime = totalTime;
    }

    public String getExpectedAudience() {
        return ExpectedAudience;
    }

    public void setExpectedAudience(String expectedAudience) {
        ExpectedAudience = expectedAudience;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}
