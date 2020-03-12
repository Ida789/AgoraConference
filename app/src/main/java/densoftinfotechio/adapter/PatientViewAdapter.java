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
    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());




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

        /*if(doctorModels.get(position).getSessionType().equalsIgnoreCase("Video")){
            holder.tv_status.setBackground(context.getResources().getDrawable(R.drawable.video_call));
        }*/

        Log.d("call value ", doctorModels.get(position).getSessionType() + " " + doctorModels.get(position).getInitiateCall());
        if(doctorModels.get(position).getSessionType().equalsIgnoreCase("Text") && (doctorModels.get(position).getInitiateCall() == 0)){
            holder.tv_status.setText("Start a Chat");
            holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.text_chat, 0);
            //holder.tv_status.setBackground(context.getResources().getDrawable(R.drawable.text_chat));
        }else if(doctorModels.get(position).getSessionType().equalsIgnoreCase("Audio") && (doctorModels.get(position).getInitiateCall() == 0)){
            holder.tv_status.setText("Make an Audio call");
            holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.call_audio, 0);
            //holder.tv_status.setBackground(context.getResources().getDrawable(R.drawable.call_audio));
        }else if(doctorModels.get(position).getSessionType().equalsIgnoreCase("Video") && (doctorModels.get(position).getInitiateCall() == 0)){
            holder.tv_status.setText("Make a Video Call");
            holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.video_call, 0);
            //holder.tv_status.setBackground(context.getResources().getDrawable(R.drawable.video_call));
        }

        if(doctorModels.get(position).getInitiateCall() == 1){
            holder.tv_status.setText("Calling");
            holder.tv_status.setEnabled(false);
        }else if(doctorModels.get(position).getInitiateCall() == 2){
            holder.tv_status.setText("Call Completed");
            holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.accept, 0);
            holder.tv_status.setEnabled(false);
        }else if(doctorModels.get(position).getInitiateCall() == 3){
            holder.tv_status.setText("Call Rejected");
            holder.tv_status.setTextColor(context.getResources().getColor(R.color.red));
            holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.reject, 0);
            holder.tv_status.setEnabled(false);
        }



        holder.tv_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((PatientViewActivity)context).gotoCall(doctorModels.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return doctorModels.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_date, tv_time, tv_doctor, tv_status;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_date = itemView.findViewById(R.id.tv_date);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_doctor = itemView.findViewById(R.id.tv_doctor);
            tv_status = itemView.findViewById(R.id.tv_status);
        }
    }
}
