package densoftinfotechio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

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
import densoftinfotechio.model.EventsModel;
import densoftinfotechio.model.PatientRequestsModel;
import densoftinfotechio.utilities.InternetUtils;
import densoftinfotechio.utilities.Loader;
import densoftinfotechio.videocall.openlive.activities.MainActivity;

public class WaitingActivity extends AppCompatActivity {

    TextView tv_loading, tv_join;

    Animation fade_animation;
    private DatabaseReference databaseReference;
    private SharedPreferences preferences;
    Bundle b;
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat sdftime = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    //String test_time = "10:00";
    HashMap<String, Object> startEvent = new HashMap<>();

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

            final Loader loader = new Loader(WaitingActivity.this);
            loader.startLoader();

            if (InternetUtils.getInstance(WaitingActivity.this).available()) {
                /*Intent i = new Intent(WaitingActivity.this, MainActivityv1.class);
                startActivity(i);*/
                databaseReference.child("Events").child(String.valueOf(b.getInt("doctor", 0)))
                        .child(sdf.format(Calendar.getInstance().getTime())).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //if (dataSnapshot.exists()) {
                        for (DataSnapshot children : dataSnapshot.getChildren()) {
                            databaseReference.child("Events").child(String.valueOf(b.getInt("doctor", 0)))
                                    .child(sdf.format(Calendar.getInstance().getTime())).child(children.getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    EventsModel eventsModel = dataSnapshot.getValue(EventsModel.class);

                                    if (eventsModel != null) {
                                        densoftinfotechio.videocall.openlive.Constants.event_time = eventsModel.getFromTime();

                                        if (checktimewithinrange(eventsModel.getEventDate(), eventsModel.getFromTime(), eventsModel.getTotalTime())) {
                                            if (checktime(eventsModel.getEventDate(), eventsModel.getFromTime(), eventsModel.getTotalTime())) {
                                                gotofirebase(eventsModel.getEventDate(), eventsModel.getFromTime());
                                            } else {
                                                tv_loading.setText("Sorry this event has ended ");
                                                tv_join.setVisibility(View.GONE);
                                            }
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                        loader.dismissLoader();
                        //}
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else {
                loader.dismissLoader();
                Toast.makeText(WaitingActivity.this, "Please check Internet", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void gotofirebase(final String eventdate, final String eventtime) {
        databaseReference.child("Events").child(String.valueOf(b.getInt("doctor", 0))).child(eventdate)
                .child(eventtime).child(b.getString("type", "Audience"))
                .child(String.valueOf(preferences.getInt("id", 0))).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //update start event in firebase to start an event
                /*startEvent.put("StartEvent", 1);
                databaseReference.child("Events").child(String.valueOf(b.getInt("doctor", 0))).child(eventdate)
                        .child(eventtime).child(b.getString("type", "Audience"))
                        .child(String.valueOf(preferences.getInt("id", 0))).updateChildren(startEvent);
                databaseReference.child("Events").child(String.valueOf(b.getInt("doctor", 0))).child(eventdate)
                        .child(eventtime).child(b.getString("type", "Co-Host"))
                        .child(String.valueOf(preferences.getInt("id", 0))).updateChildren(startEvent);*/


                if (!dataSnapshot.exists()/* && test_time.equalsIgnoreCase(sdftime.format(Calendar.getInstance().getTime()))*/) {
                    fade_animation = AnimationUtils.loadAnimation(WaitingActivity.this, R.anim.fade_animation);
                    tv_loading.startAnimation(fade_animation);
                    HashMap<String, Object> param = new HashMap<>();
                    param.put("EventId", preferences.getInt("id", 0));
                    param.put("PatientId", preferences.getInt("id", 0));
                    param.put("Status", 0);
                    param.put("StartEvent", 1);
                    param.put("Type", b.getString("type", "Audience"));
                    databaseReference.child("Events").child(String.valueOf(b.getInt("doctor", 0)))
                            .child(eventdate).child(eventtime)
                            .child(b.getString("type", "Audience")).child(String.valueOf(preferences.getInt("id", 0))).setValue(param);
                } else {
                    PatientRequestsModel requestsModel = dataSnapshot.getValue(PatientRequestsModel.class);
                    densoftinfotechio.videocall.openlive.Constants.channel = b.getInt("channelname", 0);
                    if (requestsModel != null) {
                        if (requestsModel.getStartEvent() == 1) {
                            if (requestsModel.getStatus() == 1) {
                                if (requestsModel.getType().equalsIgnoreCase("Co-Host")) {
                                    Intent i = new Intent(WaitingActivity.this, MainActivity.class);
                                    i.putExtra("channelname", b.getInt("channelname", 0));
                                    i.putExtra("type", "Co-Host");
                                    startActivity(i);
                                    finish();
                                } else {
                                    Intent i = new Intent(WaitingActivity.this, MainActivity.class);
                                    i.putExtra("channelname", b.getInt("channelname", 0));
                                    i.putExtra("type", "Audience");
                                    startActivity(i);
                                    finish();
                                }

                            } else {
                                if (requestsModel.getType().equalsIgnoreCase("Co-Host")) {
                                    tv_loading.setText("Please Wait while the Admin accepts your request to Join...........");
                                    fade_animation = AnimationUtils.loadAnimation(WaitingActivity.this, R.anim.fade_animation);
                                    tv_loading.startAnimation(fade_animation);
                                } else {
                                    Intent i = new Intent(WaitingActivity.this, MainActivity.class);
                                    i.putExtra("channelname", b.getInt("channelname", 0));
                                    i.putExtra("type", "Audience");
                                    startActivity(i);
                                    finish();
                                }
                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

   /* private void checktime(String geteventtime, final String role_type) {

        Calendar c = Calendar.getInstance();
        String getcurrenttime = sdftime.format(c.getTime());
        tv_loading.clearAnimation();

        try {
            Date current_time = sdftime.parse(getcurrenttime);
            Date event_time = sdftime.parse(geteventtime);

            if (event_time.compareTo(current_time) < 0) {
                Log.d("time check before ", current_time.before(event_time) + "  current time " + current_time);
                tv_loading.setText("Event will start at " + geteventtime);
                tv_join.setVisibility(View.GONE);
            } else if (event_time.compareTo(current_time) > 0 || event_time.compareTo(current_time) == 0) {
                Log.d("time check ", current_time.equals(event_time) + "  current time " + current_time);
                tv_loading.setText("Event is on ");
                tv_join.setVisibility(View.VISIBLE);
            } else {
                *//*if(checktime(current_time, checktotal_timeof_event())){

                }*//*
                tv_loading.setText("Sorry this event has ended " + geteventtime);
                tv_join.setVisibility(View.GONE);
            }

            tv_join.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    densoftinfotechio.videocall.openlive.Constants.channel = b.getInt("channelname", 0);
                    //if (role_type.equalsIgnoreCase("Co-Host")) {
                    Intent i = new Intent(WaitingActivity.this, MainActivity.class);
                    i.putExtra("channelname", b.getInt("channelname", 0));
                    i.putExtra("type", role_type);
                    startActivity(i);
                    finish();
                    *//*} else {
                        Intent i = new Intent(WaitingActivity.this, MainActivity.class);
                        i.putExtra("channelname", b.getInt("channelname", 0));
                        i.putExtra("type", "Audience");
                        startActivity(i);
                        finish();
                    }*//*
                }
            });

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }*/

    private boolean checktime(String eventdate, String eventtime, long totaltime) {
        try {
            Calendar c = Calendar.getInstance();
            String getcurrenttime = sdftime.format(c.getTime());
            Date d1 = sdftime.parse(getcurrenttime);

            Date d2 = sdftime.parse(eventdate + " " + eventtime);
            d2.setMinutes(d2.getMinutes() + (int) totaltime);
            Calendar calendar2 = Calendar.getInstance();
            Log.d("time plus 5 is ", "\n current time " + d1.toString() + " \n event time " + d2.toString());

            if (d2.compareTo(d1) < 0) {
                return false;
            } else {
                return true;
            }


        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checktimewithinrange(String eventdate, String eventtime, long totaltime) {
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
            Log.d("value is ", true + "");
            Date x = calendar3.getTime();
            if (x.after(calendar1.getTime()) && x.before(calendar2.getTime())) {
                Log.d("value is ", true + "");
                return true;
            } else {
                Log.d("value is ", false + "");
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
}
