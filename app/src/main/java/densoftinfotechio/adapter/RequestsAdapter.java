package densoftinfotechio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import densoftinfotechio.agora.openlive.R;
import densoftinfotechio.model.PatientRequestsModel;
import densoftinfotechio.videocall.openlive.activities.LiveActivityEvent;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {

    Context context;
    ArrayList<PatientRequestsModel> requestsModels = new ArrayList<>();

    public RequestsAdapter(Context context, ArrayList<PatientRequestsModel> requestsModels){
        this.context = context;
        this.requestsModels = requestsModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.requests_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.tv_patientid.setText(requestsModels.get(position).getPatientId() + "");

        if(requestsModels.get(position).getStatus() == 1){
            accepted(holder);
        }else if(requestsModels.get(position).getStatus() == 2){
            denied(holder);
        }

        holder.tv_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LiveActivityEvent)context).call_status(1, requestsModels.get(position).getPatientId()); //accept
                accepted(holder);
            }
        });
        holder.tv_deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LiveActivityEvent)context).call_status(2, requestsModels.get(position).getPatientId()); //reject
                denied(holder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestsModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_patientid, tv_accept, tv_deny;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_patientid = itemView.findViewById(R.id.tv_patientid);
            tv_accept = itemView.findViewById(R.id.tv_accept);
            tv_deny = itemView.findViewById(R.id.tv_deny);
        }
    }

    private void accepted(ViewHolder holder){
        holder.tv_accept.setText("Accepted");
        holder.tv_accept.setBackgroundResource(R.drawable.rounded_acceptrequest);
        holder.tv_deny.setBackgroundResource(R.drawable.rounded_gray_background);
        holder.tv_accept.setEnabled(false);
        holder.tv_deny.setEnabled(false);
    }

    private void denied(ViewHolder holder){
        holder.tv_deny.setText("Denied");
        holder.tv_deny.setBackgroundResource(R.drawable.rounded_deniedrequest);
        holder.tv_accept.setBackgroundResource(R.drawable.rounded_gray_background);
        holder.tv_accept.setEnabled(false);
        holder.tv_deny.setEnabled(false);
    }
}
