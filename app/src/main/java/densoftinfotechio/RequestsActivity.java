package densoftinfotechio;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import densoftinfotechio.adapter.RequestsAdapter;
import densoftinfotechio.agora.openlive.R;
import densoftinfotechio.classes.Constants;
import densoftinfotechio.model.PatientRequestsModel;

public class RequestsActivity extends AppCompatActivity {

    RecyclerView recycler_view_requests;
    ArrayList<PatientRequestsModel> requestsModels = new ArrayList<>();
    RequestsAdapter requestsAdapter;
    LinearLayoutManager layoutManager;
    private DatabaseReference databaseReference;
    private SharedPreferences preferences;
    private Calendar c = Calendar.getInstance();
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    private String ROLE_CO_HOST = "Co-Host";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        recycler_view_requests = findViewById(R.id.recycler_view_requests);
        layoutManager = new LinearLayoutManager(RequestsActivity.this);
        recycler_view_requests.setLayoutManager(layoutManager);

        preferences = PreferenceManager.getDefaultSharedPreferences(RequestsActivity.this);

        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.firebasedatabasename);

        requestsAdapter = new RequestsAdapter(RequestsActivity.this, requestsModels);
        recycler_view_requests.setAdapter(requestsAdapter);

        if(preferences!=null && preferences.contains("id")){
            databaseReference.child("Events").child(String.valueOf(preferences.getInt("id", 0))).child(simpleDateFormat.format(c.getTime())).child(ROLE_CO_HOST).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        requestsModels.clear();
                        for(DataSnapshot children: dataSnapshot.getChildren()){
                            databaseReference.child("Events").child(String.valueOf(preferences.getInt("id", 0))).child(simpleDateFormat.format(c.getTime())).child(ROLE_CO_HOST).child(children.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()) {
                                        PatientRequestsModel requestsModel = dataSnapshot.getValue(PatientRequestsModel.class);
                                        requestsModels.add(requestsModel);
                                    }

                                    requestsAdapter.notifyDataSetChanged();

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
    }

    public void call_status(String i, int patientId) {
        HashMap<String, Object> param = new HashMap<>();
        param.put("Status", i);
        databaseReference.child("Events").child(String.valueOf(preferences.getInt("id", 0))).child(simpleDateFormat.format(c.getTime())).child(ROLE_CO_HOST).child(String.valueOf(patientId)).updateChildren(param);
    }
}
