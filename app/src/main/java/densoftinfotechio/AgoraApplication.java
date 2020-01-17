package densoftinfotechio;

import android.app.Application;
import android.content.SharedPreferences;

import densoftinfotechio.audiocall.openlive.voice.only.model.CurrentUserSettings;
import densoftinfotechio.audiocall.openlive.voice.only.model.WorkerThread;
import densoftinfotechio.videocall.openlive.Constants;
import densoftinfotechio.videocall.openlive.rtc.EngineConfig;
import densoftinfotechio.videocall.openlive.stats.StatsManager;
import densoftinfotechio.videocall.openlive.utils.FileUtil;
import densoftinfotechio.videocall.openlive.utils.PrefManager;
import densoftinfotechio.agora.openlive.R;
import densoftinfotechio.videocall.openlive.rtc.AgoraEventHandler;
import densoftinfotechio.videocall.openlive.rtc.EventHandler;
import io.agora.rtc.RtcEngine;

public class AgoraApplication extends Application{
    private RtcEngine mRtcEngine;
    private EngineConfig mGlobalConfig = new EngineConfig();
    private AgoraEventHandler mHandler = new AgoraEventHandler();
    private StatsManager mStatsManager = new StatsManager();
    private WorkerThread mWorkerThread;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            mRtcEngine = RtcEngine.create(getApplicationContext(), getString(R.string.private_app_id), mHandler);
            mRtcEngine.setChannelProfile(io.agora.rtc.Constants.CHANNEL_PROFILE_GAME);
            mRtcEngine.enableVideo();
            mRtcEngine.enableWebSdkInteroperability(true);
            mRtcEngine.setLogFile(FileUtil.initializeLogFile(this));
        } catch (Exception e) {
            e.printStackTrace();
        }

        initConfig();
    }

    public synchronized void initWorkerThread() {
        if (mWorkerThread == null) {
            mWorkerThread = new WorkerThread(getApplicationContext());
            mWorkerThread.start();

            mWorkerThread.waitForReady();
        }
    }

    public synchronized WorkerThread getWorkerThread() {
        return mWorkerThread;
    }

    public synchronized void deInitWorkerThread() {
        mWorkerThread.exit();
        try {
            mWorkerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mWorkerThread = null;
    }

    public static final CurrentUserSettings mAudioSettings = new CurrentUserSettings();

    private void initConfig() {
        SharedPreferences pref = PrefManager.getPreferences(getApplicationContext() );
        mGlobalConfig.setVideoDimenIndex(pref.getInt(
                Constants.PREF_RESOLUTION_IDX, Constants.DEFAULT_PROFILE_IDX));

        boolean showStats = pref.getBoolean(Constants.PREF_ENABLE_STATS, false);
        mGlobalConfig.setIfShowVideoStats(showStats);
        mStatsManager.enableStats(showStats);
    }

    public EngineConfig engineConfig() { return mGlobalConfig; }

    public RtcEngine rtcEngine() { return mRtcEngine; }

    public StatsManager statsManager() { return mStatsManager; }

    public void registerEventHandler(EventHandler handler) { mHandler.addHandler(handler); }

    public void removeEventHandler(EventHandler handler) { mHandler.removeHandler(handler); }

    @Override
    public void onTerminate() {
        super.onTerminate();
        RtcEngine.destroy();
    }
}
