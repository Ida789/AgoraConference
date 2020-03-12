package densoftinfotechio.model;

public class DoctorModel {
    private int AppointmentId = 0;
    private int PatientId = 0;
    private int Channel = 0;
    private String Date = "";
    private String Day = "";
    private int DoctorId = 0;
    private String SessionTime = "";
    private String SessionType = "";
    private int InitiateCall = 0;
    private String Talktime = "";

    public DoctorModel() {
    }

    public int getAppointmentId() {
        return AppointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        AppointmentId = appointmentId;
    }

    public int getPatientId() {
        return PatientId;
    }

    public void setPatientId(int patientId) {
        PatientId = patientId;
    }

    public int getChannel() {
        return Channel;
    }

    public void setChannel(int channel) {
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

    public int getDoctorId() {
        return DoctorId;
    }

    public void setDoctorId(int doctorId) {
        DoctorId = doctorId;
    }

    public String getSessionTime() {
        return SessionTime;
    }

    public void setSessionTime(String sessionTime) {
        SessionTime = sessionTime;
    }

    public String getSessionType() {
        return SessionType;
    }

    public void setSessionType(String sessionType) {
        SessionType = sessionType;
    }

    public int getInitiateCall() {
        return InitiateCall;
    }

    public void setInitiateCall(int initiateCall) {
        InitiateCall = initiateCall;
    }

    public String getTalktime() {
        return Talktime;
    }

    public void setTalktime(String talktime) {
        Talktime = talktime;
    }
}
