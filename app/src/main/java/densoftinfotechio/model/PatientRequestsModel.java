package densoftinfotechio.model;

public class PatientRequestsModel {
    private String PatientId = "";
    private String Status = "";
    private String StartEvent = "";

    public PatientRequestsModel(){

    }

    public String getPatientId() {
        return PatientId;
    }

    public void setPatientId(String patientId) {
        PatientId = patientId;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getStartEvent() {
        return StartEvent;
    }

    public void setStartEvent(String startEvent) {
        StartEvent = startEvent;
    }
}
