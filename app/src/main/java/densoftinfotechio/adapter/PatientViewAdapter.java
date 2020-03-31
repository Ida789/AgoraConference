package densoftinfotechio.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import densoftinfotechio.model.DoctorModel;
import densoftinfotechio.PatientViewActivity;
import densoftinfotechio.agora.openlive.R;

public class PatientViewAdapter extends RecyclerView.Adapter<PatientViewAdapter.MyViewHolder> {

    Context context;
    ArrayList<DoctorModel> doctorModels = new ArrayList<>();
    SimpleDateFormat sdftime = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    public PatientViewAdapter(Context context, ArrayList<DoctorModel> doctorModels) {
        this.context = context;
        this.doctorModels = doctorModels;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patientview_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.tv_date.setText(doctorModels.get(position).getDate());
        holder.tv_time.setText(doctorModels.get(position).getSessionTime());
        holder.tv_doctor.setText(String.valueOf(doctorModels.get(position).getDoctorId()));
        holder.tv_sessiontype.setText("Dcotor");

        if (checktimewithinrange(doctorModels.get(position).getDate(), doctorModels.get(position).getSessionTime(),
                doctorModels.get(position).getTalktime(), holder)) {
            //if ((doctorModels.get(position).getInitiateCall() == 0) || doctorModels.get(position).getInitiateCall() == 1) {
                if (doctorModels.get(position).getSessionType().equalsIgnoreCase("Text")) {
                    holder.tv_status.setText("Start a Chat");
                    holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.text_chat, 0);
                } else if (doctorModels.get(position).getSessionType().equalsIgnoreCase("Audio")) {
                    holder.tv_status.setText("Make an Audio call");
                    holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.call_audio, 0);
                } else if (doctorModels.get(position).getSessionType().equalsIgnoreCase("Video")) {
                    holder.tv_status.setText("Make a Video Call");
                    holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.video_call, 0);
                }

            /*} else if (doctorModels.get(position).getInitiateCall() == 1) {
                holder.tv_status.setText("Calling");
                holder.tv_status.setEnabled(false);
            } else if (doctorModels.get(position).getInitiateCall() == 2) {
                holder.tv_status.setText("Call Completed");
                holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.accept, 0);
                holder.tv_status.setEnabled(false);
            } else if (doctorModels.get(position).getInitiateCall() == 3) {
                holder.tv_status.setText("Call Rejected");
                holder.tv_status.setTextColor(context.getResources().getColor(R.color.red));
                holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.reject, 0);
                holder.tv_status.setEnabled(false);
            }*/
        }else {
            if (doctorModels.get(position).getInitiateCall() == 2 || doctorModels.get(position).getInitiateCall() == 1) {
                holder.tv_status.setText("Completed");
                holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.accept, 0);
                holder.tv_status.setEnabled(false);
            }
        }


        holder.tv_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    ((PatientViewActivity) context).gotoCall(doctorModels.get(position));
                    Log.d("doctor ", doctorModels.get(position) + "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return doctorModels.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_date, tv_time, tv_doctor, tv_status, tv_sessiontype;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_date = itemView.findViewById(R.id.tv_date);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_doctor = itemView.findViewById(R.id.tv_doctor);
            tv_status = itemView.findViewById(R.id.tv_status);
            tv_sessiontype = itemView.findViewById(R.id.tv_sessiontype);
        }
    }


    private boolean checktimewithinrange(String eventdate, String eventtime, long totaltime, MyViewHolder holder) {
        try {
            Date time1 = sdftime.parse(eventdate + " " + eventtime);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(time1);
            //calendar1.add(Calendar.DATE, 1);

            Date time2 = time1;
            time2.setMinutes(time2.getMinutes() + (int) totaltime);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(time2);
            //calendar2.add(Calendar.DATE, 1);

            Date d = sdftime.parse(sdftime.format(Calendar.getInstance().getTime()));
            Calendar calendar3 = Calendar.getInstance();
            calendar3.setTime(d);
            //calendar3.add(Calendar.DATE, 1);
            Date x = calendar3.getTime();

            if (x.after(calendar1.getTime()) && x.before(calendar2.getTime())) {
                Log.d("value is ", true + "");
                return true;
            } else if(x.before(calendar2.getTime())){
                holder.tv_status.setText("Missed");
                holder.tv_status.setEnabled(false);
                return false;
            }else {
                Log.d("value is ", false + "");
                return false;
            }


        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

}
