package densoftinfotechio.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import densoftinfotechio.DoctorViewActivity;
import densoftinfotechio.agora.openlive.R;
import densoftinfotechio.model.PatientModel;

public class DoctorViewAdapter extends RecyclerView.Adapter<DoctorViewAdapter.ViewHolder> {

    Context context;
    ArrayList<PatientModel> patientModels = new ArrayList<>();

    public DoctorViewAdapter(Context context, ArrayList<PatientModel> patientModels ){
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
        //holder.tv_sessiontype.setText(patientModels.get(position).getSessionType());
        holder.tv_sessiontype.setText("Doctor");

        Log.d("call value ", patientModels.get(position).getSessionType() + " " + patientModels.get(position).getInitiateCall());
        if(patientModels.get(position).getSessionType().equalsIgnoreCase("Text") && patientModels.get(position).getInitiateCall() == 0){
            holder.tv_status.setText("Start a Chat");
            holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.text_chat, 0);
            //holder.tv_status.setBackground(context.getResources().getDrawable(R.drawable.text_chat));
        }else if(patientModels.get(position).getSessionType().equalsIgnoreCase("Audio") && patientModels.get(position).getInitiateCall() == 0){
            holder.tv_status.setText("Make an Audio call");
            holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.call_audio, 0);
            //holder.tv_status.setBackground(context.getResources().getDrawable(R.drawable.call_audio));
        }else if(patientModels.get(position).getSessionType().equalsIgnoreCase("Video") && patientModels.get(position).getInitiateCall() == 0){
            holder.tv_status.setText("Make a Video Call");
            holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.video_call, 0);
            //holder.tv_status.setBackground(context.getResources().getDrawable(R.drawable.video_call));
        }


        if(patientModels.get(position).getInitiateCall() == 1){
            holder.tv_status.setText("Calling");
            holder.tv_status.setEnabled(false);
        }else if(patientModels.get(position).getInitiateCall() == 2){
            holder.tv_status.setText("Call Completed");
            holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.accept, 0);
            holder.tv_status.setEnabled(false);
        }else if(patientModels.get(position).getInitiateCall() == 3){
            holder.tv_status.setText("Call Rejected");
            holder.tv_status.setTextColor(context.getResources().getColor(R.color.red));
            holder.tv_status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.reject, 0);
            holder.tv_status.setEnabled(false);
        }

        holder.tv_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DoctorViewActivity)context).gotoCall(patientModels.get(position).getDoctorId(), patientModels.get(position).getDate(), patientModels.get(position).getPatientId(),
                        patientModels.get(position).getChannel(), patientModels.get(position).getSessionType());
            }
        });
    }

    @Override
    public int getItemCount() {
        return patientModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
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
}
