package densoftinfotechio.model;

public class DoctorModel {
    String Channel = "";
    String Date = "";
    String Day = "";
    String DoctorId = "";
    String PatientId = "";
    String SessionType = "";
    String SessionTime = "";
    String InitiateCall = "";
    String Talktime = "";

    public DoctorModel() {
    }

    public String getChannel() {
        return Channel;
    }

    public void setChannel(String channel) {
        Channel = channel;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getDay() {
        return Day;
    }

    public void setDay(String day) {
        Day = day;
    }

    public String getDoctorId() {
        return DoctorId;
    }

    public void setDoctorId(String doctorId) {
        DoctorId = doctorId;
    }

    public String getPatientId() {
        return PatientId;
    }

    public void setPatientId(String patientId) {
        PatientId = patientId;
    }

    public String getSessionType() {
        return SessionType;
    }

    public void setSessionType(String sessionType) {
        SessionType = sessionType;
    }

    public String getSessionTime() {
        return SessionTime;
    }

    public void setSessionTime(String sessionTime) {
        SessionTime = sessionTime;
    }

    public String getInitiateCall() {
        return InitiateCall;
    }

    public void setInitiateCall(String initiateCall) {
        InitiateCall = initiateCall;
    }

    public String getTalktime() {
        return Talktime;
    }

    public void setTalktime(String talktime) {
        Talktime = talktime;
    }
}
