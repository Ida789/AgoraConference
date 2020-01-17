package densoftinfotechio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import densoftinfotechio.model.DoctorScheduleModel;
import densoftinfotechio.AppointmentsActivity;
import densoftinfotechio.agora.openlive.R;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    Context context;
    ArrayList<DoctorScheduleModel> appointments = new ArrayList<>();
    int shift = 0;

    public AppointmentAdapter(Context context, ArrayList<DoctorScheduleModel> appointments, int shift){
        this.context = context;
        this.appointments = appointments;
        this.shift = shift;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sessions_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        if(shift == 1) {
            if(!appointments.get(position).getMorningShift().trim().equals("")){
                holder.tv_sessiontime.setText(appointments.get(position).getMorningShift());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((AppointmentsActivity)context).chooseList(appointments.get(position).getMorningShift());
                    }
                });
            }else{
                holder.tv_sessiontime.setText("No slots available for this time");
            }

        }else if(shift == 2){
            if(!appointments.get(position).getAfternoonShift().trim().equals("")) {
                holder.tv_sessiontime.setText(appointments.get(position).getAfternoonShift());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((AppointmentsActivity) context).chooseList(appointments.get(position).getAfternoonShift());
                    }
                });
            }else{
                holder.tv_sessiontime.setText("No slots available for this time");
            }
        }else if(shift == 3){
            if(!appointments.get(position).getEveningShift().trim().equals("")) {
                holder.tv_sessiontime.setText(appointments.get(position).getEveningShift());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((AppointmentsActivity) context).chooseList(appointments.get(position).getEveningShift());
                    }
                });
            }else{
                holder.tv_sessiontime.setText("No slots available for this time");
            }
        }else{
            holder.tv_sessiontime.setText("No slots available for this time");
        }


    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_sessiontime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_sessiontime = itemView.findViewById(R.id.tv_sessiontime);
        }
    }
}
