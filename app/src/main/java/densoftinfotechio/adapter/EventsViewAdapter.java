package densoftinfotechio.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import densoftinfotechio.agora.openlive.R;
import densoftinfotechio.model.EventsModel;
import densoftinfotechio.videocall.openlive.activities.MainActivity;

public class EventsViewAdapter extends RecyclerView.Adapter<EventsViewAdapter.MyViewHolder> {

    Context context;
    ArrayList<EventsModel> eventsModels = new ArrayList<>();
    private SharedPreferences preferences;
    private DatabaseReference databaseReference;
    private String activity = "";
    SimpleDateFormat sdftime = new SimpleDateFormat("HH:mm");

    public EventsViewAdapter(Context context, ArrayList<EventsModel> eventsModels, String activity) {
        this.context = context;
        this.eventsModels = eventsModels;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        databaseReference = FirebaseDatabase.getInstance().getReference("BookedAppointments");
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.events_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int i) {
        holder.tv_event_date.setText(eventsModels.get(i).getEventDate());
        holder.tv_event_name.setText(eventsModels.get(i).getEventName());
        holder.from_time.setText(eventsModels.get(i).getFromTime());
        holder.to_time.setText(eventsModels.get(i).getTotalTime() + " mins");

        if (preferences != null && preferences.contains("logindoctor")) {
            holder.linearlayout_rightsofdcotor.setVisibility(View.VISIBLE);

            if (activity.equalsIgnoreCase("EventsViewActivity")) {
                holder.tv_startevent.setVisibility(View.VISIBLE);
                holder.tv_inviteascohost.setVisibility(View.VISIBLE);
                holder.tv_inviteasaudience.setVisibility(View.VISIBLE);
                //holder.tv_inviteascohost.setVisibility(View.GONE);
                //holder.tv_inviteasaudience.setVisibility(View.GONE);

                if (checktime(eventsModels.get(i).getFromTime())) {
                    holder.tv_startevent.setVisibility(View.VISIBLE);
                } else {
                    holder.tv_startevent.setVisibility(View.INVISIBLE);
                }

                holder.tv_startevent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        firebase_update_startevent(i);

                    }
                });

            } else {
                holder.tv_startevent.setVisibility(View.GONE);
                holder.tv_inviteascohost.setVisibility(View.VISIBLE);
                holder.tv_inviteasaudience.setVisibility(View.VISIBLE);
            }

        } else {
            holder.linearlayout_rightsofdcotor.setVisibility(View.GONE);
        }

        holder.tv_inviteascohost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                        /*sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey, kindly join the link as Co-Host " + "https://blog.ida.org.in/?doctor="+eventsModels.get(i).getEventId()
                                + "&channel=" + eventsModels.get(i).getEventId() + "&type=Co-Host" + "&eventdate=" + eventsModels.get(i).getEventDate()
                                + "&eventtime=" + eventsModels.get(i).getFromTime());*/
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey, kindly join the link as Co-Host " + "https://blog.ida.org.in/?channel="
                        + eventsModels.get(i).getEventId() + "&type=Co-Host");
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, null);

                if (sendIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(shareIntent);
                } else {
                    Toast.makeText(context.getApplicationContext(), "No app found to share", Toast.LENGTH_SHORT).show();
                }

            }
        });

        holder.tv_inviteasaudience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                        /*sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey, kindly join the link as Audience " + "https://blog.ida.org.in/?doctor="+eventsModels.get(i).getEventId()
                                + "&channel=" + eventsModels.get(i).getEventId() + "&type=Audience" + "&eventdate=" + eventsModels.get(i).getEventDate()
                                + "&eventtime=" + eventsModels.get(i).getFromTime());*/
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey, kindly join the link as Audience " + "https://blog.ida.org.in/?channel="
                        + eventsModels.get(i).getEventId() + "&type=Audience");
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, null);

                if (sendIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(shareIntent);
                } else {
                    Toast.makeText(context.getApplicationContext(), "No app found to share", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private void firebase_update_startevent(final int pos) {
        databaseReference.child("Events").child(String.valueOf(eventsModels.get(pos).getDoctorId())).child(eventsModels.get(pos).getEventDate())
                .child(eventsModels.get(pos).getFromTime()).child("Co-Host").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> param_update = new HashMap<>();
                param_update.put("StartEvent", 1);
                if (dataSnapshot.exists()) {

                    for (DataSnapshot user_requests : dataSnapshot.getChildren()) {
                        databaseReference.child("Events").child(String.valueOf(eventsModels.get(pos).getDoctorId())).child(eventsModels.get(pos).getEventDate())
                                .child(eventsModels.get(pos).getFromTime()).child("Co-Host").child(user_requests.getKey()).updateChildren(param_update);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("Events").child(String.valueOf(eventsModels.get(pos).getDoctorId())).child(eventsModels.get(pos).getEventDate())
                .child(eventsModels.get(pos).getFromTime()).child("Audience").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> param_update = new HashMap<>();
                param_update.put("StartEvent", 1);
                param_update.put("Status", 2); //audience
                if (dataSnapshot.exists()) {

                    for (DataSnapshot user_requests : dataSnapshot.getChildren()) {
                        databaseReference.child("Events").child(String.valueOf(eventsModels.get(pos).getDoctorId())).child(eventsModels.get(pos).getEventDate())
                                .child(eventsModels.get(pos).getFromTime()).child("Audience").child(user_requests.getKey()).updateChildren(param_update);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        densoftinfotechio.videocall.openlive.Constants.channel = eventsModels.get(pos).getEventId();
        Intent ilive = new Intent(context, MainActivity.class);
        ilive.putExtra("channelname", eventsModels.get(pos).getEventId());
        ilive.putExtra("type", "Host");
        context.startActivity(ilive);
    }

    @Override
    public int getItemCount() {
        return eventsModels.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_event_date, tv_event_name, from_time, to_time, tv_inviteascohost, tv_inviteasaudience, tv_startevent;
        LinearLayout linearlayout_rightsofdcotor;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_event_date = itemView.findViewById(R.id.tv_event_date);
            tv_event_name = itemView.findViewById(R.id.tv_event_name);
            from_time = itemView.findViewById(R.id.from_time);
            to_time = itemView.findViewById(R.id.to_time);
            linearlayout_rightsofdcotor = itemView.findViewById(R.id.linearlayout_rightsofdcotor);
            tv_inviteascohost = itemView.findViewById(R.id.tv_inviteascohost);
            tv_inviteasaudience = itemView.findViewById(R.id.tv_inviteasaudience);
            tv_startevent = itemView.findViewById(R.id.tv_startevent);
        }
    }

    private boolean checktime(String geteventtime) {

        Calendar c = Calendar.getInstance();
        String getcurrenttime = sdftime.format(c.getTime());

        try {
            Date current_time = sdftime.parse(getcurrenttime);
            Date event_time = sdftime.parse(geteventtime);

            if (current_time.before(event_time)) {
                return false;
            } else if (current_time.equals(event_time) || current_time.after(event_time)) {
                return true;
            } else {
                return false;
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

}
