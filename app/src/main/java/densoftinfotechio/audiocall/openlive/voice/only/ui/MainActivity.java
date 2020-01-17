package densoftinfotechio.audiocall.openlive.voice.only.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import densoftinfotechio.agora.openlive.R;
import io.agora.rtc.Constants;
import densoftinfotechio.audiocall.openlive.voice.only.model.ConstantApp;

public class MainActivity extends BaseActivity {

    EditText v_room;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_broadcast_activity_main);

        v_room = findViewById(R.id.room_name);
        Log.d("called method ", " onCreate");



    }

    @Override
    protected void initUIandEvent() {

        /*v_room.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean isEmpty = TextUtils.isEmpty(s.toString());
                findViewById(R.id.button_join).setEnabled(!isEmpty);
            }
        });*/

        Log.d("called method ", " initUIandEvent");
        /*String lastChannelName = vSettings().mChannelName;
        if (!TextUtils.isEmpty(lastChannelName)) {
            v_room.setText(lastChannelName);
            v_room.setSelection(lastChannelName.length());
        }*/

        Bundle b = getIntent().getExtras();
        if(b!=null && b.containsKey("channelname")) {
            v_room.setText(b.getString("channelname").replace("-", "").trim());
            String room = v_room.getText().toString();
            forwardToLiveRoom(Constants.CLIENT_ROLE_BROADCASTER, room);
        }else{
            Toast.makeText(MainActivity.this, "Please try in a while.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void deInitUIandEvent() {
        Log.d("called method ", " deInitUIandEvent");
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void onClickJoin(View view) {






        /*// show dialog to choose role
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.msg_choose_role);
        builder.setNegativeButton(R.string.label_audience, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.forwardToLiveRoom(Constants.CLIENT_ROLE_AUDIENCE);
            }
        });
        builder.setPositiveButton(R.string.label_broadcaster, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.forwardToLiveRoom(Constants.CLIENT_ROLE_BROADCASTER);
            }
        });
        AlertDialog dialog = builder.create();

        dialog.show();*/
    }

    public void forwardToLiveRoom(final int cRole, final String channelname) {
        /*final EditText v_room = (EditText) findViewById(R.id.room_name);
        v_room.setText(channelname);
        String room = v_room.getText().toString();*/

        new CountDownTimer(1100, 1000) {
            public void onFinish() {
                Intent i = new Intent(MainActivity.this,LiveRoomActivity.class);
                i.putExtra(ConstantApp.ACTION_KEY_CROLE, cRole);
                i.putExtra(ConstantApp.ACTION_KEY_ROOM_NAME, channelname);
                startActivity(i);
                finish();
            }

            public void onTick(long millisUntilFinished) {
            }

        }.start();
    }
}
