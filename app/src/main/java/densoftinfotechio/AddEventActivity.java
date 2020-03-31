package densoftinfotechio;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import densoftinfotechio.agora.openlive.R;
import densoftinfotechio.classes.Constants;

public class AddEventActivity extends AppCompatActivity {

    EditText et_eventname, et_event_date, et_fromtime, et_totime, et_audience;
    Button btn_addevent;
    private DatabaseReference databaseReference;
    private SharedPreferences preferences;
    int day_cal = 0, month_cal = 0, year_cal = 0;
    int hour_cal = 0, minute_cal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        et_eventname = findViewById(R.id.et_eventname);
        et_event_date = findViewById(R.id.et_event_date);
        et_fromtime = findViewById(R.id.et_fromtime);
        et_totime = findViewById(R.id.et_totime);
        btn_addevent = findViewById(R.id.btn_addevent);
        et_audience = findViewById(R.id.et_audience);

        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.firebasedatabasename);
        preferences = PreferenceManager.getDefaultSharedPreferences(AddEventActivity.this);
        final Calendar c = Calendar.getInstance();

        et_event_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                day_cal = c.get(Calendar.DAY_OF_MONTH);
                month_cal = c.get(Calendar.MONTH);
                year_cal = c.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddEventActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        if ((month + 1) < 10) {
                            et_event_date.setText(day + "-0" + (month + 1) + "-" + year);
                        } else {
                            et_event_date.setText(day + "-" + (month + 1) + "-" + year);
                        }

                    }
                }, year_cal, month_cal, day_cal);

                datePickerDialog.show();

            }
        });

        et_fromtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                time_picker(c, 0);
            }
        });

        btn_addevent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    if (preferences != null && preferences.contains("id")) {
                        add_event_to_firebase(preferences.getInt("id", 0), et_event_date.getText().toString(),
                                et_fromtime.getText().toString().replace(" AM", "").replace(" PM", ""));
                    }
                }

            }
        });
    }

    private boolean validate() {
        if (et_eventname.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter the event name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (et_event_date.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter the event date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (et_fromtime.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter the time to start the event", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (et_totime.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter the time to end the event", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (et_audience.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter the expected audience to attend the event", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;

    }

    private void time_picker(Calendar c, final int i) {
        hour_cal = c.get(Calendar.HOUR_OF_DAY);
        minute_cal = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(AddEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {

                if (i == 0) {
                    if (hour < 12) {
                        et_fromtime.setText(String.format("%02d:%02d AM", hour, minute));
                    } else {
                        et_fromtime.setText(String.format("%02d:%02d PM", hour, minute));
                    }
                } else {
                    if (hour < 12) {
                        et_totime.setText(String.format("%02d:%02d AM", hour, minute));
                    } else {
                        et_totime.setText(String.format("%02d:%02d PM", hour, minute));
                    }
                }
            }
        }, hour_cal, minute_cal, false);
        timePickerDialog.show();
    }

    private void add_event_to_firebase(final int doctorid, final String event_date, final String event_time) {
        databaseReference.child("Events").child(String.valueOf(doctorid)).child(event_date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> event_param = new HashMap<>();
                event_param.put("FromTime", event_time);
                event_param.put("TotalTime", Long.parseLong(et_totime.getText().toString()));
                event_param.put("EventName", et_eventname.getText().toString());
                event_param.put("EventId", doctorid);
                event_param.put("EventDate", event_date);
                event_param.put("ExpectedAudience", Long.parseLong(et_audience.getText().toString()));
                event_param.put("DoctorId", doctorid);

                if (!dataSnapshot.exists()) {
                    databaseReference.child("Events").child(String.valueOf(doctorid)).child(event_date).child(event_time).setValue(event_param);
                    Toast.makeText(getApplicationContext(), "Event Added", Toast.LENGTH_SHORT).show();
                } else {
                    databaseReference.child("Events").child(String.valueOf(doctorid)).child(event_date).child(event_time).updateChildren(event_param);
                    Toast.makeText(getApplicationContext(), "Event Updated", Toast.LENGTH_SHORT).show();

                }
                clear_data();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void clear_data() {
        et_eventname.setText("");
        et_event_date.setText("");
        et_fromtime.setText("");
        et_totime.setText("");
        et_audience.setText("");
        et_eventname.requestFocus();
    }
}
