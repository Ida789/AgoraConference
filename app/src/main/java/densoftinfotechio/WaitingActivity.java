package densoftinfotechio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import densoftinfotechio.agora.openlive.R;
import densoftinfotechio.classes.Constants;
import densoftinfotechio.model.PatientRequestsModel;
import densoftinfotechio.videocall.openlive.activities.MainActivity;

public class WaitingActivity extends AppCompatActivity {

    TextView tv_loading, tv_join;

    Animation fade_animation;
    private DatabaseReference databaseReference;
    private SharedPreferences preferences;
    Bundle b;
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat sdftime = new SimpleDateFormat("HH:mm");
    String test_time = "17:45";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        tv_loading = findViewById(R.id.tv_loading);
        tv_join = findViewById(R.id.tv_join);

        preferences = PreferenceManager.getDefaultSharedPreferences(WaitingActivity.this);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.firebasedatabasename);

        b = getIntent().getExtras();

        if (b != null && b.containsKey("channelname") && b.containsKey("type") && preferences != null && preferences.contains("id")) {
            databaseReference.child("Events").child(b.getString("doctor", "")).child(sdf.format(Calendar.getInstance().getTime()))
                    .child(test_time).child(b.getString("type", "Audience"))
                    .child(preferences.getString("id", "")).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()/* && test_time.equalsIgnoreCase(sdftime.format(Calendar.getInstance().getTime()))*/) {
                        fade_animation = AnimationUtils.loadAnimation(WaitingActivity.this, R.anim.fade_animation);
                        tv_loading.startAnimation(fade_animation);
                        HashMap<String, Object> param = new HashMap<>();
                        param.put("PatientId", preferences.getString("id", ""));
                        param.put("Status", "0");
                        param.put("StartEvent", "0");
                        param.put("Type", b.getString("type", "Audience"));
                        databaseReference.child("Events").child(b.getString("doctor", ""))
                                .child(sdf.format(Calendar.getInstance().getTime())).child(test_time/*sdftime.format(Calendar.getInstance().getTime())*/)
                                .child(b.getString("type", "Audience")).child(preferences.getString("id", "")).setValue(param);
                    } else {
                        PatientRequestsModel requestsModel = dataSnapshot.getValue(PatientRequestsModel.class);

                        if (requestsModel != null) {
                            if (requestsModel.getStartEvent().equalsIgnoreCase("1")) {
                                if (requestsModel.getStatus().equalsIgnoreCase("1")
                                        && requestsModel.getType().equalsIgnoreCase("Co-Host")) {
                                    Intent i = new Intent(WaitingActivity.this, MainActivity.class);
                                    i.putExtra("channelname", b.getString("channelname", ""));
                                    i.putExtra("type", "Co-Host");
                                    startActivity(i);
                                    finish();
                                } else/* if (requestsModel != null && requestsModel.getStatus().equalsIgnoreCase("2"))*/ {
                                    Intent i = new Intent(WaitingActivity.this, MainActivity.class);
                                    i.putExtra("channelname", b.getString("channelname", ""));
                                    i.putExtra("type", "Audience");
                                    startActivity(i);
                                    finish();
                                }
                            } /*else if (requestsModel.getType().equalsIgnoreCase("Co-Host")) {

                                fade_animation = AnimationUtils.loadAnimation(WaitingActivity.this, R.anim.fade_animation);
                                tv_loading.startAnimation(fade_animation);

                            }*/else{
                                checktime(test_time, requestsModel.getType());
                            }
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void checktime(String geteventtime, final String role_type) {

        Calendar c = Calendar.getInstance();
        String getcurrenttime = sdftime.format(c.getTime());
        tv_loading.clearAnimation();

        try {
            Date current_time = sdftime.parse(getcurrenttime);
            Date event_time = sdftime.parse(geteventtime);

            if (current_time.before(event_time)) {
                Log.d("time check before ", current_time.before(event_time) + "  current time " + current_time);
                tv_loading.setText("Event will start at " + geteventtime);
                tv_join.setVisibility(View.GONE);
            } else if (current_time.equals(event_time) || current_time.after(event_time)) {
                Log.d("time check ", current_time.equals(event_time) + "  current time " + current_time);
                tv_loading.setText("Event is on ");
                tv_join.setVisibility(View.VISIBLE);
            } else {
                /*if(checktime(current_time, checktotal_timeof_event())){

                }*/
                tv_loading.setText("Sorry this event has ended " + geteventtime);
                tv_join.setVisibility(View.GONE);
            }

            tv_join.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (role_type.equalsIgnoreCase("Co-Host")) {
                        Intent i = new Intent(WaitingActivity.this, MainActivity.class);
                        i.putExtra("channelname", b.getString("channelname", ""));
                        i.putExtra("type", "Co-Host");
                        startActivity(i);
                        finish();
                    } else {
                        Intent i = new Intent(WaitingActivity.this, MainActivity.class);
                        i.putExtra("channelname", b.getString("channelname", ""));
                        i.putExtra("type", "Audience");
                        startActivity(i);
                        finish();
                    }
                }
            });

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /*private String checktotal_timeof_event(String eventtime) {

        Date d = null;
        try {
            d = sdftime.parse(eventtime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.MINUTE, 20);
        String newTime = sdftime.format(cal.getTime());
        Log.d("old time ", eventtime + " new time " + newTime);
        return newTime;
    }*/
}

/*
databaseReference.child("Events").child(b.getString("doctor", "")).child(b.getString("eventdate", ""))
        .child(b.getString("eventtime", "")).child("Requests")
        .child(preferences.getString("id", "")).addValueEventListener(new ValueEventListener() {
@Override
public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if (!dataSnapshot.exists()) {
        HashMap<String, Object> param = new HashMap<>();
        param.put("PatientId", preferences.getString("id", ""));
        param.put("Status", "0");
        param.put("StartEvent", "0");
        databaseReference.child("Events").child(b.getString("doctor", ""))
        .child(b.getString("eventdate", "")).child(b.getString("eventtime", ""))
        .child("Requests").child(preferences.getString("id", "")).setValue(param);
        } else {
        PatientRequestsModel requestsModel = dataSnapshot.getValue(PatientRequestsModel.class);
        if (requestsModel != null && requestsModel.getStartEvent().equalsIgnoreCase("1")) {
        if (requestsModel != null && requestsModel.getStatus().equalsIgnoreCase("1")) {
        Intent i = new Intent(WaitingActivity.this, MainActivity.class);
        i.putExtra("channelname", b.getString("channelname", ""));
        i.putExtra("type", "Co-Host");
        startActivity(i);
        finish();
        } else if (requestsModel != null && requestsModel.getStatus().equalsIgnoreCase("2")) {
        Intent i = new Intent(WaitingActivity.this, MainActivity.class);
        i.putExtra("channelname", b.getString("channelname", ""));
        i.putExtra("type", "Audience");
        startActivity(i);
        finish();
        }
        } else {
        //Case when user
        checktime(b.getString("eventtime", ""));
        }


        }
        }

@Override
public void onCancelled(@NonNull DatabaseError databaseError) {

        }
        });
*/
