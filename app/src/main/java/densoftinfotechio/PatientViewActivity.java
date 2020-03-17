package densoftinfotechio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import densoftinfotechio.adapter.PatientViewAdapter;
import densoftinfotechio.backgroundservices.BackgroundServiceNotification;
import densoftinfotechio.model.PatientModel;
import densoftinfotechio.videocall.openlive.Constants;
import densoftinfotechio.videocall.openlive.activities.MainActivity;
import densoftinfotechio.model.DoctorModel;
import densoftinfotechio.agora.openlive.R;


public class PatientViewActivity extends AppCompatActivity {

    EditText et_patient_id;
    TextView tv_search, tv_bookappointment;
    RecyclerView recyclerview_patient;
    PatientViewAdapter patientViewAdapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<DoctorModel> doctorModels = new ArrayList<>();
    private DatabaseReference databaseReference;
    SharedPreferences preferences;
    SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_view);




        et_patient_id = findViewById(R.id.et_patient_id);
        tv_search = findViewById(R.id.tv_search);
        recyclerview_patient = findViewById(R.id.recyclerview_patient);
        tv_bookappointment = findViewById(R.id.tv_bookappointment);

        layoutManager = new LinearLayoutManager(PatientViewActivity.this);
        recyclerview_patient.setLayoutManager(layoutManager);

        preferences = PreferenceManager.getDefaultSharedPreferences(PatientViewActivity.this);
        edit = preferences.edit();
        if (preferences != null && preferences.contains("id")) {
            et_patient_id.setText(String.valueOf(preferences.getInt("id", 0)));
            //delete_this_method();
        }

        databaseReference = FirebaseDatabase.getInstance().getReference(densoftinfotechio.classes.Constants.firebasedatabasename);
        startService(new Intent(getApplicationContext(), BackgroundServiceNotification.class));

        databaseReference.child("PatientList").child(et_patient_id.getText().toString()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (final DataSnapshot datechildren : dataSnapshot.getChildren()) {

                        databaseReference.child("PatientList").child(String.valueOf(preferences.getInt("id", 0))).child(datechildren.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (final DataSnapshot doctorchildren : dataSnapshot.getChildren()) {
                                    doctorModels.clear();
                                    databaseReference.child("PatientList").child(String.valueOf(preferences.getInt("id", 0))).child(datechildren.getKey()).child(doctorchildren.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            DoctorModel doctorModel = dataSnapshot.getValue(DoctorModel.class);
                                            doctorModels.add(doctorModel);

                                            if (doctorModels != null) {
                                                patientViewAdapter = new PatientViewAdapter(PatientViewActivity.this, doctorModels);
                                                recyclerview_patient.setAdapter(patientViewAdapter);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        tv_bookappointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PatientViewActivity.this, AppointmentsActivity.class);
                startActivity(i);
            }
        });

    }

    private void delete_this_method() {
        Intent i = new Intent(PatientViewActivity.this, densoftinfotechio.realtimemessaging.agora.activity.LoginActivity.class);
        i.putExtra("accountname", preferences.getInt("id", 0));
        i.putExtra("friendname", "3000");
        startActivity(i);
    }

    public void gotoCall(final DoctorModel doctorModel) {
        databaseReference.child("DoctorList").child(String.valueOf(doctorModel.getDoctorId())).child(doctorModel.getDate()).child(String.valueOf(doctorModel.getPatientId())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    HashMap<String, Object> paramupdate = new HashMap<>();
                    paramupdate.put("InitiateCall", 1);
                    databaseReference.child("DoctorList").child(String.valueOf(doctorModel.getDoctorId())).child(doctorModel.getDate()).child(String.valueOf(doctorModel.getPatientId())).updateChildren(paramupdate);
                    //databaseReference.child("PatientList").child(patient).child(date).child(doctor).updateChildren(paramupdate);

                    Constants.doctorId = doctorModel.getDoctorId();
                    Constants.patientId = doctorModel.getPatientId();
                    Constants.channel = doctorModel.getChannel();
                    Log.d("call value sent ", Constants.doctorId + " " + Constants.patientId);

                    if (doctorModel.getSessionType().equalsIgnoreCase("Video")) {
                        Intent i = new Intent(PatientViewActivity.this, MainActivity.class);
                        i.putExtra("channelname", doctorModel.getChannel());
                        startActivity(i);
                        //finish();

                    } else if (doctorModel.getSessionType().equalsIgnoreCase("Audio")) {
                        Intent i = new Intent(PatientViewActivity.this, densoftinfotechio.audiocall.openlive.voice.only.ui.MainActivity.class);
                        i.putExtra("channelname", doctorModel.getChannel());
                        startActivity(i);
                        //finish();
                    } else if (doctorModel.getSessionType().equalsIgnoreCase("Text")) {
                        Intent i = new Intent(PatientViewActivity.this, densoftinfotechio.realtimemessaging.agora.activity.LoginActivity.class);
                        i.putExtra("accountname", doctorModel.getPatientId());
                        i.putExtra("friendname", doctorModel.getDoctorId());
                        startActivity(i);
                        //finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.patientmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                edit.clear();
                edit.apply();

                Intent i = new Intent(PatientViewActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
                break;

            case R.id.event:
                Intent i2 = new Intent(PatientViewActivity.this, EventsViewActivity.class);
                startActivity(i2);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void show_call() {

        databaseReference.child("PatientList").child(String.valueOf(preferences.getInt("id", 0))).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (final DataSnapshot patients : dataSnapshot.getChildren()) {
                        databaseReference.child("PatientList").child(String.valueOf(preferences.getInt("id", 0))).child(patients.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (final DataSnapshot patient_list : dataSnapshot.getChildren()) {
                                    doctorModels.clear();
                                    databaseReference.child("PatientList").child(String.valueOf(preferences.getInt("id", 0))).child(patients.getKey()).child(patient_list.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                DoctorModel doctorModel = dataSnapshot.getValue(DoctorModel.class);
                                                if (doctorModel != null) {
                                                    if (doctorModel.getInitiateCall() == 1) {
                                                        Constants.doctorId = doctorModel.getDoctorId();
                                                        Constants.patientId = doctorModel.getPatientId();
                                                        Constants.channel = doctorModel.getChannel();
                                                        //stopService(new Intent(DoctorViewActivity.this, BackgroundServiceNotification.class));
                                                        Intent i = new Intent(PatientViewActivity.this, CallingActivity.class);
                                                        i.putExtra("doctorid", doctorModel.getDoctorId());
                                                        i.putExtra("channelname", doctorModel.getChannel());
                                                        i.putExtra("dateofcall", doctorModel.getDate());
                                                        i.putExtra("sessiontype", doctorModel.getSessionType());
                                                        startActivity(i);
                                                        finish();
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

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {

        Constants.callinitiatedInActivity = 1;
        Log.d("Call initiated ", " in onResume " + Constants.callinitiatedInActivity);
        show_call();


        super.onResume();
    }

}
