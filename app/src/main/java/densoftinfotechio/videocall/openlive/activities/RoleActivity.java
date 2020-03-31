package densoftinfotechio.videocall.openlive.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import densoftinfotechio.agora.openlive.R;
import io.agora.rtc.Constants;

public class RoleActivity extends BaseActivity {

    Bundle b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_broadcast_activity_choose_role);
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        RelativeLayout layout = findViewById(R.id.role_title_layout);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) layout.getLayoutParams();
        params.height += mStatusBarHeight;
        layout.setLayoutParams(params);

        layout = findViewById(R.id.role_content_layout);
        params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
        params.topMargin = (mDisplayMetrics.heightPixels -
                layout.getMeasuredHeight()) * 3 / 7;
        layout.setLayoutParams(params);

        b = getIntent().getExtras();
        if(b!=null && b.containsKey("type")){
            //deep link flow

            if(b.getString("type","").equalsIgnoreCase("Host") || b.getString("type","").equalsIgnoreCase("Co-Host")) {
                gotoLiveEventActivity(Constants.CLIENT_ROLE_BROADCASTER);
            }else if(b.getString("type","").equalsIgnoreCase("Audience")){
                gotoLiveEventActivity(Constants.CLIENT_ROLE_AUDIENCE);
            }
        }else if(b!=null && b.containsKey("channelname")){
            //normal flow
            gotoLiveActivity(Constants.CLIENT_ROLE_BROADCASTER);
        }else{
            gotoLiveActivity(Constants.CLIENT_ROLE_AUDIENCE);
        }
    }

    public void onJoinAsBroadcaster(View view) {
        gotoLiveActivity(Constants.CLIENT_ROLE_BROADCASTER);
    }

    public void onJoinAsAudience(View view) {
        gotoLiveActivity(Constants.CLIENT_ROLE_AUDIENCE);
    }

    private void gotoLiveActivity(int role) {
        //normal flow
        Intent intent = new Intent(getIntent());
        intent.putExtra(densoftinfotechio.videocall.openlive.Constants.KEY_CLIENT_ROLE, role);
        intent.setClass(getApplicationContext(), LiveActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoLiveEventActivity(int role) {
        //deep link flow
        Intent intent = new Intent(getIntent());
        intent.putExtra(densoftinfotechio.videocall.openlive.Constants.KEY_CLIENT_ROLE, role);
        intent.setClass(getApplicationContext(), LiveActivityEvent.class);
        startActivity(intent);
        finish();
    }

    public void onBackArrowPressed(View view) {
        finish();
    }
}
