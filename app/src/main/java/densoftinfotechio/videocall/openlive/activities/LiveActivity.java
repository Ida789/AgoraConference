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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.preference.PreferenceManager;
import densoftinfotechio.AgoraApplication;
import densoftinfotechio.DoctorViewActivity;
import densoftinfotechio.PatientViewActivity;
import densoftinfotechio.agora.openlive.R;
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
import io.agora.rtm.RtmClient;

public class LiveActivity extends RtcBaseActivity {
    private static final String TAG = LiveActivity.class.getSimpleName();

    private VideoGridContainer mVideoGridContainer;
    private ImageView mMuteAudioBtn;
    private ImageView mMuteVideoBtn;
    private SharedPreferences sharedPreferences;
    private ChatManager mChatManager;

    private VideoEncoderConfiguration.VideoDimensions mVideoDimension;
    TextView tv_time;
    private boolean mIsInChat = false;
    private RtmClient mRtmClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.video_broadcast_activity_live_room);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LiveActivity.this);
        initUI();
        tv_time = findViewById(R.id.tv_time);
        initData();

        mChatManager = AgoraApplication.the().getChatManager();
        mRtmClient = mChatManager.getRtmClient();

    }


    private void initUI() {
        TextView roomName = findViewById(R.id.live_room_name);
        roomName.setText(config().getChannelName());
        roomName.setSelected(true);

        initUserIcon();

        int role = getIntent().getIntExtra(
                densoftinfotechio.videocall.openlive.Constants.KEY_CLIENT_ROLE,
                Constants.CLIENT_ROLE_AUDIENCE);
        boolean isBroadcaster = (role == Constants.CLIENT_ROLE_BROADCASTER);

        mMuteVideoBtn = findViewById(R.id.live_btn_mute_video);
        mMuteVideoBtn.setActivated(isBroadcaster);

        mMuteAudioBtn = findViewById(R.id.live_btn_mute_audio);
        mMuteAudioBtn.setActivated(isBroadcaster);

        ImageView beautyBtn = findViewById(R.id.live_btn_beautification);
        beautyBtn.setActivated(true);
        rtcEngine().setBeautyEffectOptions(beautyBtn.isActivated(),
                densoftinfotechio.videocall.openlive.Constants.DEFAULT_BEAUTY_OPTIONS);
        rtcEngine().enableWebSdkInteroperability(true);

        mVideoGridContainer = findViewById(R.id.live_video_grid_layout);
        mVideoGridContainer.setStatsManager(statsManager());

        rtcEngine().setClientRole(role);
        rtcEngine().enableVideo();
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
        Log.d("agora method ", " onJoinChannelSuccess Called " + elapsed);
    }

    @Override
    public void onUserJoined(final int uid, int elapsed) {
        // Do nothing at the moment
        Log.d("agora method ", " onUserJoined Called " + elapsed);
    }

    @Override
    public void onUserOffline(final int uid, int reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeRemoteUser(uid);
                Log.d("agora method ", " onUserOffline Called");
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

        /*if (sharedPreferences != null && sharedPreferences.contains("logindoctor")) {
            Intent i = new Intent(LiveActivity.this, DoctorViewActivity.class);
            startActivity(i);
            finish();

        } else {
            Intent i = new Intent(LiveActivity.this, PatientViewActivity.class);
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

        //Intent i = new Intent(LiveActivity.this, densoftinfotechio.realtimemessaging.agora.activity.LoginActivity.class);

        if (sharedPreferences != null && sharedPreferences.contains("logindoctor")) {
            doLogin(densoftinfotechio.videocall.openlive.Constants.patientId, densoftinfotechio.videocall.openlive.Constants.doctorId);
            Log.d("here flow ", "part 3 Live Activity doctor");
            //i.putExtra("accountname", densoftinfotechio.videocall.openlive.Constants.doctorId);
            //i.putExtra("friendname", densoftinfotechio.videocall.openlive.Constants.patientId);
        } else {
            doLogin(densoftinfotechio.videocall.openlive.Constants.doctorId, densoftinfotechio.videocall.openlive.Constants.patientId);
            Log.d("here flow ", "part 3 Live Activity patient");
            //i.putExtra("accountname", densoftinfotechio.videocall.openlive.Constants.patientId);
            //i.putExtra("friendname", densoftinfotechio.videocall.openlive.Constants.doctorId);
        }
        //startActivity(i);
        //finish();

    }

    public void onMuteAudioClicked(View view) {
        if (!mMuteVideoBtn.isActivated()) return;

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
        try {
            statsManager().clearAllData();
            doLogout();
            if (sharedPreferences != null && sharedPreferences.contains("logindoctor")) {
                Intent i = new Intent(LiveActivity.this, DoctorViewActivity.class);
                startActivity(i);
                finish();
            } else {
                Intent i = new Intent(LiveActivity.this, PatientViewActivity.class);
                startActivity(i);
                finish();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //super.onBackPressed();
    }

    public void onScreenSharingClicked(View view) {
        //Intent i = new Intent(LiveActivity.this, BroadcasterActivity.class);
        //startActivity(i);
    }


    private void doLogin(final int friendname, final int accountname) {
        mIsInChat = true;
        mRtmClient.login(null, String.valueOf(accountname), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                Log.i(TAG, "login success");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(LiveActivity.this, SelectionActivity.class);
                        intent.putExtra(MessageUtil.INTENT_EXTRA_USER_ID, String.valueOf(accountname));
                        Log.d("muser id ", accountname + " live activity" );
                        intent.putExtra("friendname", friendname);
                        intent.putExtra("accountname", accountname);
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
                        if(errorInfo.getErrorCode() ==8){
                            Intent intent = new Intent(LiveActivity.this, SelectionActivity.class);
                            intent.putExtra(MessageUtil.INTENT_EXTRA_USER_ID, String.valueOf(accountname));
                            Log.d("muser id ", accountname + " live activity" );
                            intent.putExtra("friendname", friendname);
                            intent.putExtra("accountname", accountname);
                            startActivity(intent);
                            finish();
                        }

                    }
                });
            }
        });
    }

    private void doLogout() {
        mRtmClient.logout(null);
        MessageUtil.cleanMessageListBeanList();
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        if (mIsInChat) {
            doLogout();
        }
    }*/
}
