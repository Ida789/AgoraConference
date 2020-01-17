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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        tv_loading = findViewById(R.id.tv_loading);
        tv_join = findViewById(R.id.tv_join);
        fade_animation = AnimationUtils.loadAnimation(WaitingActivity.this, R.anim.fade_animation);
        tv_loading.startAnimation(fade_animation);

        preferences = PreferenceManager.getDefaultSharedPreferences(WaitingActivity.this);
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.firebasedatabasename);

        b = getIntent().getExtras();

        if (b != null && b.containsKey("channelname") && b.containsKey("type") && preferences != null && preferences.contains("id")) {
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
        }
        //blink();
    }

    private void blink() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int timeToBlink = 500;    //in ms
                try {
                    Thread.sleep(timeToBlink);
                } catch (Exception e) {
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (tv_loading.getVisibility() == View.VISIBLE) {
                            tv_loading.setVisibility(View.INVISIBLE);
                        } else {
                            tv_loading.setVisibility(View.VISIBLE);
                        }
                        blink();
                    }
                });
            }
        }).start();
    }

    private void checktime(String eventtime) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Calendar c = Calendar.getInstance();
        String currenttime = sdf.format(c.getTime());
        tv_loading.clearAnimation();

        try {
            Date current_time = sdf.parse(currenttime);
            Date event_time = sdf.parse(eventtime);

            if(current_time.before(event_time)){
                Log.d("time check before ", current_time.before(event_time) + "  current time " + current_time);
                tv_loading.setText("Event will start at " + eventtime);
                tv_join.setVisibility(View.GONE);
            }else if(current_time.equals(event_time)){

                Log.d("time check ", currenttime.equals(eventtime) + "  current time " + current_time);
                tv_loading.setText("Event is on " + eventtime);
                tv_join.setVisibility(View.VISIBLE);
            }else{
                checktotal_timeof_event(eventtime, sdf);
                tv_loading.setText("Sorry this event has ended " + eventtime);
                tv_join.setVisibility(View.GONE);

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String checktotal_timeof_event(String eventtime, SimpleDateFormat sdf) {

        Date d = null;
        try {
            d = sdf.parse(eventtime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.MINUTE, 60);
        String newTime = sdf.format(cal.getTime());
        Log.d("old time ", eventtime + " new time " + newTime);
        return newTime;
    }
}
