package densoftinfotechio.model;

public class DoctorScheduleModel {
    private String MorningShift = "";
    private String AfternoonShift = "";
    private String EveningShift = "";

    public DoctorScheduleModel() {
    }

    public DoctorScheduleModel(String shift, int flag){
        if(flag == 1){
            this.MorningShift = shift;
        }else if(flag == 2){
            this.AfternoonShift = shift;
        }else if(flag == 3){
            this.EveningShift = shift;
        }
    }

    public String getMorningShift() {
        return MorningShift;
    }

    public void setMorningShift(String morningShift) {
        MorningShift = morningShift;
    }

    public String getAfternoonShift() {
        return AfternoonShift;
    }

    public void setAfternoonShift(String afternoonShift) {
        AfternoonShift = afternoonShift;
    }

    public String getEveningShift() {
        return EveningShift;
    }

    public void setEveningShift(String eveningShift) {
        EveningShift = eveningShift;
    }
}
