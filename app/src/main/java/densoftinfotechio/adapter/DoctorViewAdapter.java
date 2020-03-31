package densoftinfotechio.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import densoftinfotechio.DoctorViewActivity;
import densoftinfotechio.agora.openlive.R;
import densoftinfotechio.model.PatientModel;

public class DoctorViewAdapter extends RecyclerView.Adapter<DoctorViewAdapter.ViewHolder> {

    Context context;
    ArrayList<PatientModel> patientModels = new ArrayList<>();
    SimpleDateFormat sdftime = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    public DoctorViewAdapter(Context context, ArrayList<PatientModel> patientModels) {
        this.context = context;
        this.patientModels = patientModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.doctorview_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.tv_date.setText(patientModels.get(position).getDate());
        holder.tv_time.setText(patientModels.get(position).getSessionTime());
        holder.tv_patientid.setText(String.valueOf(patientModels.get(position).getPatientId()));
        holder.tv_sessiontype.setText("Patient");

        Log.d("call value ", patientModels.get(position).getSessionType() + " " + patientModels.get(position).getInitiateCall());

        if(checktimewithinrange(patientModels.get(position).getDate(), patientModels.get(position).getSessionTime(),
                patientModels.get(position).getTalktime(), holder)){
            //if (patientModels.get(position).getInitiateCall() == 0) {
                if(patientModels.get(position).getSessionType().equalsIgnoreCase("Text")) {
                    holder.tv_status.setText("Start a Chat");
                    holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.text_chat, 0);
                }else if (patientModels.get(position).getSessionType().equalsIgnoreCase("Audio") && patientModels.get(position).getInitiateCall() == 0) {
                    holder.tv_status.setText("Make an Audio call");
                    holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.call_audio, 0);
                } else if (patientModels.get(position).getSessionType().equalsIgnoreCase("Video") && patientModels.get(position).getInitiateCall() == 0) {
                    holder.tv_status.setText("Make a Video Call");
                    holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.video_call, 0);
                }
            /*} else if (patientModels.get(position).getInitiateCall() == 1) {
                holder.tv_status.setText("Calling");
                holder.tv_status.setEnabled(false);
            } else if (patientModels.get(position).getInitiateCall() == 2) {
                holder.tv_status.setText("Call Completed");
                holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.accept, 0);
                holder.tv_status.setEnabled(false);
            } else if (patientModels.get(position).getInitiateCall() == 3) {
                holder.tv_status.setText("Call Rejected");
                holder.tv_status.setTextColor(context.getResources().getColor(R.color.red));
                holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.reject, 0);
                holder.tv_status.setEnabled(false);
            }*/
        }else {
            if (patientModels.get(position).getInitiateCall() == 2 || patientModels.get(position).getInitiateCall()==1) {
                holder.tv_status.setText("Completed");
                holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.accept, 0);
                holder.tv_status.setEnabled(false);
            }
        }


        holder.tv_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ((DoctorViewActivity) context).gotoCall(patientModels.get(position));
                    Log.d("patient ", patientModels.get(position) + "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return patientModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_date, tv_time, tv_sessiontype, tv_patientid, tv_accept, tv_reject, tv_status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_date = itemView.findViewById(R.id.tv_date);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_sessiontype = itemView.findViewById(R.id.tv_sessiontype);
            tv_patientid = itemView.findViewById(R.id.tv_patientid);

            tv_status = itemView.findViewById(R.id.tv_status);
        }
    }

    private boolean checktimewithinrange(String eventdate, String eventtime, long totaltime, ViewHolder holder){
        try {
            Date time1 = sdftime.parse(eventdate + " " + eventtime);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(time1);
            //calendar1.add(Calendar.DATE, 1);

            Date time2 = time1;
            time2.setMinutes(time2.getMinutes() + (int)totaltime);
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
            }else if(x.before(calendar2.getTime())){
                holder.tv_status.setText("Missed");
                holder.tv_status.setEnabled(false);
                return false;
            }else{
                Log.d("value is ", false + "");
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

}
