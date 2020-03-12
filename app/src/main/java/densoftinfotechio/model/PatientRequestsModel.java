package densoftinfotechio.model;

public class PatientRequestsModel {

    private int EventId = 0;
    private int PatientId = 0;
    private int Status = 0;
    private int StartEvent = 0;
    private String Type = "";

    public PatientRequestsModel(){

    }

    public int getEventId() {
        return EventId;
    }

    public void setEventId(int eventId) {
        EventId = eventId;
    }

    public int getPatientId() {
        return PatientId;
    }

    public void setPatientId(int patientId) {
        PatientId = patientId;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    public int getStartEvent() {
        return StartEvent;
    }

    public void setStartEvent(int startEvent) {
        StartEvent = startEvent;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }
}
