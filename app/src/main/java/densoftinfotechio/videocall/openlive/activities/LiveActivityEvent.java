package densoftinfotechio.videocall.openlive.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import densoftinfotechio.AgoraApplication;
import densoftinfotechio.DoctorViewActivity;
import densoftinfotechio.PatientViewActivity;
import densoftinfotechio.adapter.RequestsAdapter;
import densoftinfotechio.agora.openlive.R;
import densoftinfotechio.model.EventsModel;
import densoftinfotechio.model.PatientRequestsModel;
import densoftinfotechio.realtimemessaging.agora.activity.SelectionActivity;
import densoftinfotechio.realtimemessaging.agora.rtmtutorial.ChatManager;
import densoftinfotechio.realtimemessaging.agora.utils.MessageUtil;

import densoftinfotechio.videocall.openlive.stats.LocalStatsData;
import densoftinfotechio.videocall.openlive.stats.RemoteStatsData;
import densoftinfotechio.videocall.openlive.stats.StatsData;
import densoftinfotechio.videocall.openlive.ui.VideoGridContainer;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmChannel;
import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmClientListener;

public class LiveActivityEvent extends RtcBaseActivity {
    private static final String TAG = LiveActivityEvent.class.getSimpleName();

    private VideoGridContainer mVideoGridContainer;
    private ImageView mMuteAudioBtn;
    private ImageView mMuteVideoBtn;
    private SharedPreferences sharedPreferences;

    private VideoEncoderConfiguration.VideoDimensions mVideoDimension;
    TextView tv_time;

    RecyclerView recycler_view_requests;
    ArrayList<PatientRequestsModel> requestsModels = new ArrayList<>();
    RequestsAdapter requestsAdapter;
    LinearLayoutManager layoutManager;
    private DatabaseReference databaseReference;
    private Calendar c = Calendar.getInstance();
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private SimpleDateFormat simpleDateFormat_time = new SimpleDateFormat("HH:mm", Locale.getDefault());
    //String test_time = "10:12";
    private String ROLE_CO_HOST = "Co-Host";
    RelativeLayout bottom_container;

    private ChatManager mChatManager;
    private RtmClient mRtmClient;
    private RtmClientListener mClientListener;
    private RtmChannel mRtmChannel;
    private int mChannelMemberCount = 1;

    private boolean mIsInChat = false;
    int test_channel = 1200;
    int role = 2; //by default audience
    ImageView live_btn_switch_camera, live_btn_beautification, live_btn_more, live_btn_mute_audio, live_btn_mute_video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.video_broadcast_activity_live_event_room);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LiveActivityEvent.this);
        tv_time = findViewById(R.id.tv_time);
        recycler_view_requests = findViewById(R.id.recycler_view_requests);
        bottom_container = findViewById(R.id.bottom_container);
        live_btn_switch_camera = findViewById(R.id.live_btn_switch_camera);
        live_btn_beautification = findViewById(R.id.live_btn_beautification);
        live_btn_more = findViewById(R.id.live_btn_more);
        live_btn_mute_audio = findViewById(R.id.live_btn_mute_audio);
        live_btn_mute_video = findViewById(R.id.live_btn_mute_video);

        initUI();
        initData();

        mChatManager = AgoraApplication.the().getChatManager();
        mRtmClient = mChatManager.getRtmClient();

        if(sharedPreferences!=null && sharedPreferences.contains("logindoctor")){
            recycler_view_requests.setVisibility(View.VISIBLE);
            bottom_container.setVisibility(View.VISIBLE);

            layoutManager = new LinearLayoutManager(LiveActivityEvent.this);
            recycler_view_requests.setLayoutManager(layoutManager);

            get_co_hosts();

        }/*else{
            if(role == 1){
                bottom_container.setVisibility(View.VISIBLE);
                //role == 1 broadcaster and role == 2 audience
            }else{
                recycler_view_requests.setVisibility(View.GONE);
                live_btn_switch_camera.setVisibility(View.GONE);
                live_btn_beautification.setVisibility(View.GONE);
                live_btn_more.setVisibility(View.GONE);
                live_btn_mute_audio.setVisibility(View.GONE);
                live_btn_mute_video.setVisibility(View.GONE);

                rtcEngine().muteLocalAudioStream(true);
                live_btn_mute_audio.setActivated(false);
            }
        }*/
    }


    private void get_co_hosts() {
        databaseReference = FirebaseDatabase.getInstance().getReference(densoftinfotechio.classes.Constants.firebasedatabasename);

        requestsAdapter = new RequestsAdapter(LiveActivityEvent.this, requestsModels);
        recycler_view_requests.setAdapter(requestsAdapter);

        databaseReference.child("Events").child(String.valueOf(sharedPreferences.getInt("id", 0))).child(simpleDateFormat.format(c.getTime()))
                .child(densoftinfotechio.videocall.openlive.Constants.event_time/*simpleDateFormat_time.format(c.getTime())*/).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    EventsModel eventsModel = dataSnapshot.getValue(EventsModel.class);
                    if(eventsModel!=null){
                        published_events(eventsModel.getExpectedAudience());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void published_events(final long audience){
        if(sharedPreferences!=null && sharedPreferences.contains("id")){
            databaseReference.child("Events").child(String.valueOf(sharedPreferences.getInt("id", 0))).child(simpleDateFormat.format(c.getTime()))
                    .child(densoftinfotechio.videocall.openlive.Constants.event_time/*simpleDateFormat_time.format(c.getTime())*/).child(ROLE_CO_HOST).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        requestsModels.clear();
                        for(DataSnapshot children: dataSnapshot.getChildren()){
                            databaseReference.child("Events").child(String.valueOf(sharedPreferences.getInt("id", 0)))
                                    .child(simpleDateFormat.format(c.getTime())).child(densoftinfotechio.videocall.openlive.Constants.event_time/*simpleDateFormat_time.format(c.getTime())*/)
                                    .child(ROLE_CO_HOST).child(children.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void initUI() {
        TextView roomName = findViewById(R.id.live_room_name);
        roomName.setText(config().getChannelName());
        roomName.setSelected(true);

        initUserIcon();

        int role = getIntent().getIntExtra(
                densoftinfotechio.videocall.openlive.Constants.KEY_CLIENT_ROLE,
                Constants.CLIENT_ROLE_AUDIENCE);
        boolean isBroadcaster =  (role == Constants.CLIENT_ROLE_BROADCASTER);

        mMuteVideoBtn = findViewById(R.id.live_btn_mute_video);
        mMuteVideoBtn.setActivated(isBroadcaster);

        mMuteAudioBtn = findViewById(R.id.live_btn_mute_audio);
        mMuteAudioBtn.setActivated(isBroadcaster);

        ImageView beautyBtn = findViewById(R.id.live_btn_beautification);
        beautyBtn.setActivated(true);
        rtcEngine().setBeautyEffectOptions(beautyBtn.isActivated(),
                densoftinfotechio.videocall.openlive.Constants.DEFAULT_BEAUTY_OPTIONS);

        mVideoGridContainer = findViewById(R.id.live_video_grid_layout);
        mVideoGridContainer.setStatsManager(statsManager());

        rtcEngine().setClientRole(role);
        if (isBroadcaster) startBroadcast();
    }

    private void initUserIcon() {
        Bitmap origin = BitmapFactory.decodeResource(getResources(), R.drawable.fake_user_icon);
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), origin);
        drawable.setCircular(true);
        ImageView iconView = findViewById(R.id.live_name_board_icon);
        iconView.setImageDrawable(drawable);
    }

    private void initData() {
        mVideoDimension = densoftinfotechio.videocall.openlive.Constants.VIDEO_DIMENSIONS[
                config().getVideoDimenIndex()];
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        RelativeLayout topLayout = findViewById(R.id.live_room_top_layout);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) topLayout.getLayoutParams();
        params.height = mStatusBarHeight + topLayout.getMeasuredHeight();
        topLayout.setLayoutParams(params);
        topLayout.setPadding(0, mStatusBarHeight, 0, 0);
    }

    private void startBroadcast() {
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        SurfaceView surface = prepareRtcVideo(0, true);
        mVideoGridContainer.addUserVideoSurface(0, surface, true);
        mMuteAudioBtn.setActivated(true);
    }

    private void stopBroadcast() {
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
        removeRtcVideo(0, true);
        mVideoGridContainer.removeUserVideo(0, true);
        mMuteAudioBtn.setActivated(false);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        // Do nothing at the moment
        Log.d("agora method ",  " onJoinChannelSuccess Called " + elapsed);
    }

    @Override
    public void onUserJoined(final int uid, int elapsed) {
        // Do nothing at the moment
        Log.d("agora method ",  " onUserJoined Called " + elapsed);
    }

    @Override
    public void onUserOffline(final int uid, int reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeRemoteUser(uid);
                Log.d("agora method ",  " onUserOffline Called");
            }
        });
    }

    @Override
    public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                renderRemoteUser(uid);
            }
        });
    }

    private void renderRemoteUser(int uid) {
        SurfaceView surface = prepareRtcVideo(uid, false);
        mVideoGridContainer.addUserVideoSurface(uid, surface, false);
    }

    private void removeRemoteUser(int uid) {
        removeRtcVideo(uid, false);
        mVideoGridContainer.removeUserVideo(uid, false);
    }

    @Override
    public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {
        if (!statsManager().isEnabled()) return;

        LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
        if (data == null) return;

        data.setWidth(mVideoDimension.width);
        data.setHeight(mVideoDimension.height);
        data.setFramerate(stats.sentFrameRate);
    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {
        if (!statsManager().isEnabled()) return;

        LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
        if (data == null) return;

        data.setLastMileDelay(stats.lastmileDelay);
        data.setVideoSendBitrate(stats.txVideoKBitRate);
        data.setVideoRecvBitrate(stats.rxVideoKBitRate);
        data.setAudioSendBitrate(stats.txAudioKBitRate);
        data.setAudioRecvBitrate(stats.rxAudioKBitRate);
        data.setCpuApp(stats.cpuAppUsage);
        data.setCpuTotal(stats.cpuAppUsage);
        data.setSendLoss(stats.txPacketLossRate);
        data.setRecvLoss(stats.rxPacketLossRate);
    }

    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
        if (!statsManager().isEnabled()) return;

        StatsData data = statsManager().getStatsData(uid);
        if (data == null) return;

        data.setSendQuality(statsManager().qualityToString(txQuality));
        data.setRecvQuality(statsManager().qualityToString(rxQuality));
    }

    @Override
    public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setWidth(stats.width);
        data.setHeight(stats.height);
        data.setFramerate(stats.rendererOutputFrameRate);
        data.setVideoDelay(stats.delay);
    }

    @Override
    public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setAudioNetDelay(stats.networkTransportDelay);
        data.setAudioNetJitter(stats.jitterBufferDelay);
        data.setAudioLoss(stats.audioLossRate);
        data.setAudioQuality(statsManager().qualityToString(stats.quality));
    }

    @Override
    public void finish() {
        super.finish();
        //statsManager().clearAllData();
    }

    public void onLeaveClicked(View view) {

        /*if(sharedPreferences!=null && sharedPreferences.contains("logindoctor")) {
            Intent i = new Intent(LiveActivityEvent.this, DoctorViewActivity.class);
            startActivity(i);
            finish();

        }else{
            Intent i = new Intent(LiveActivityEvent.this, PatientViewActivity.class);
            startActivity(i);
            finish();
        }
        statsManager().clearAllData();*/
    }

    public void onSwitchCameraClicked(View view) {
        rtcEngine().switchCamera();
    }

    public void onBeautyClicked(View view) {
        view.setActivated(!view.isActivated());
        rtcEngine().setBeautyEffectOptions(view.isActivated(),
                densoftinfotechio.videocall.openlive.Constants.DEFAULT_BEAUTY_OPTIONS);
    }

    public void onMoreClicked(View view) {
        // Do nothing at the moment
    }

    public void onPushStreamClicked(View view) {
        // Do nothing at the moment

        try {
            doLogin(sharedPreferences.getInt("id", 0));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onMuteAudioClicked(View view) {
        if (!mMuteVideoBtn.isActivated()) return;

        Log.d("status of event ", view.isActivated() + "");
        rtcEngine().muteLocalAudioStream(view.isActivated());
        view.setActivated(!view.isActivated());
    }

    public void onMuteVideoClicked(View view) {
        if (view.isActivated()) {
            stopBroadcast();
        } else {
            startBroadcast();
        }

        view.setActivated(!view.isActivated());
    }

    @Override
    public void onBackPressed() {
        statsManager().clearAllData();
        doLogout();
        if(sharedPreferences!=null && sharedPreferences.contains("logindoctor")) {
            Intent i = new Intent(LiveActivityEvent.this, DoctorViewActivity.class);
            startActivity(i);
            finish();
        }else{
            Intent i = new Intent(LiveActivityEvent.this, PatientViewActivity.class);
            startActivity(i);
            finish();
        }

        super.onBackPressed();
    }

    public void onScreenSharingClicked(View view) {
        //Intent i = new Intent(LiveActivityEvent.this, BroadcasterActivity.class);
        //startActivity(i);
    }


    public void call_status(int i, int patientId) {
        HashMap<String, Object> param = new HashMap<>();
        param.put("Status", i);
        databaseReference.child("Events").child(String.valueOf(sharedPreferences.getInt("id", 0))).child(simpleDateFormat.format(c.getTime()))
                .child(densoftinfotechio.videocall.openlive.Constants.event_time/*simpleDateFormat_time.format(c.getTime())*/).child(ROLE_CO_HOST).child(String.valueOf(patientId)).updateChildren(param);
    }

    private void doLogin(final int accountname) {
        try{
            mIsInChat = true;


            Log.d("status of event ", accountname + "");
            mRtmClient.login(null, String.valueOf(accountname), new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void responseInfo) {
                    Log.i(TAG, "login success " + accountname);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(LiveActivityEvent.this, SelectionActivity.class);
                            intent.putExtra(MessageUtil.INTENT_EXTRA_USER_ID, String.valueOf(accountname));
                            Log.d("muser id ", accountname + " live event activity" );
                            intent.putExtra("accountname", accountname);
                            intent.putExtra("channel", test_channel);
                            startActivity(intent);
                            finish();
                        }
                    });
                }

                @Override
                public void onFailure(final ErrorInfo errorInfo) {
                    Log.i(TAG, "login failed: " + errorInfo.getErrorCode());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mIsInChat = false;
                            if(errorInfo.getErrorCode() == 8){
                                Intent intent = new Intent(LiveActivityEvent.this, SelectionActivity.class);
                                intent.putExtra(MessageUtil.INTENT_EXTRA_USER_ID, String.valueOf(accountname));
                                Log.d("muser id ", accountname + " live event activity" );
                                intent.putExtra("accountname", accountname);
                                intent.putExtra("channel", test_channel);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }



    }

    private void doLogout() {
        try {
            mRtmClient.logout(null);
            MessageUtil.cleanMessageListBeanList();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
