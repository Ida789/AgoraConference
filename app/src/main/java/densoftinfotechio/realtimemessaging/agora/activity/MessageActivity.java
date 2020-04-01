package densoftinfotechio.realtimemessaging.agora.activity;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import densoftinfotechio.AgoraApplication;
import densoftinfotechio.DoctorViewActivity;
import densoftinfotechio.LoginActivity;
import densoftinfotechio.PatientViewActivity;
import densoftinfotechio.ShowMediaActivity;
import densoftinfotechio.adapter.GalleryAdapter;
import densoftinfotechio.agora.openlive.R;
import densoftinfotechio.classes.Constants;
import densoftinfotechio.classes.DateAndTimeUtils;
import densoftinfotechio.database.DatabaseHelper;
import densoftinfotechio.realtimemessaging.agora.adapter.MessageAdapter;
import densoftinfotechio.realtimemessaging.agora.model.MessageBean;
import densoftinfotechio.realtimemessaging.agora.model.MessageListBean;
import densoftinfotechio.realtimemessaging.agora.rtmtutorial.ChatManager;
import densoftinfotechio.realtimemessaging.agora.utils.MessageUtil;
import densoftinfotechio.utilities.Loader;
import densoftinfotechio.videocall.openlive.activities.MainActivity;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmChannel;
import io.agora.rtm.RtmChannelAttribute;
import io.agora.rtm.RtmChannelListener;
import io.agora.rtm.RtmChannelMember;
import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmClientListener;
import io.agora.rtm.RtmMessage;
import io.agora.rtm.RtmStatusCode;

public class MessageActivity extends AppCompatActivity {
    private final String TAG = MessageActivity.class.getSimpleName();

    private TextView mTitleTextView;
    private EditText mMsgEditText;
    private RecyclerView mRecyclerView;
    private List<MessageBean> mMessageBeanList = new ArrayList<>();
    private MessageAdapter mMessageAdapter;

    private boolean mIsPeerToPeerMode = true;
    private String mUserId = "";
    private String mPeerId = "";
    private String mChannelName = "";
    private int mChannelMemberCount = 1;

    private ChatManager mChatManager;
    private RtmClient mRtmClient;
    private RtmClientListener mClientListener;
    private RtmChannel mRtmChannel;
    private boolean isFABOpen = false;
    FloatingActionButton fab, fab_location, fab_video, fab_photo;
    private StorageReference storageReference;
    private FirebaseStorage firebaseStorage;
    private Uri filePath;
    private SharedPreferences preferences;

    String msg = "";
    String imageEncoded;
    List<String> imagesEncodedList;
    private RecyclerView recyclerview;
    private GalleryAdapter galleryAdapter;
    LinearLayoutManager layoutManager;
    String upload_type = "image";
    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;
    boolean isTextChat = false;
    private Loader loader = new Loader(MessageActivity.this);
    private int last_pos_to_Send = 0;
    private int contentsize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agora_rtm_activity_message);
        init();
    }

    private void init() {

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab_location = (FloatingActionButton) findViewById(R.id.fab_location);
        fab_video = (FloatingActionButton) findViewById(R.id.fab_video);
        fab_photo = (FloatingActionButton) findViewById(R.id.fab_photo);

        media_options();

        preferences = PreferenceManager.getDefaultSharedPreferences(MessageActivity.this);
        /*b = getIntent().getExtras();
        if (b != null ) {
            mMessageBeanList.clear();
            if(b.containsKey("accountname") && b.containsKey("friendname")){
                accountname = b.getInt("accountname");
                friendname = b.getInt("friendname");
                mMessageBeanList.clear();
                getChat_from_sqlite(friendname);
            }else if(b.containsKey("accountname")){
                accountname = b.getInt("accountname");
                mMessageBeanList.clear();
                getChat_from_sqlite(accountname);
            }

        }*/

        //firebaseStorage = FirebaseStorage.getInstance("gs://videoconferencedemo.appspot.com");
        storageReference = FirebaseStorage.getInstance().getReference(Constants.firebasestoragename);

        layoutManager = new LinearLayoutManager(MessageActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(layoutManager);

        recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@androidx.annotation.NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d("hey ", "onScrollStateChanged");
            }

            @Override
            public void onScrolled(@androidx.annotation.NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.d("hey ", "onScrolled");
            }
        });

        mChatManager = AgoraApplication.the().getChatManager();
        mRtmClient = mChatManager.getRtmClient();
        mClientListener = new MyRtmClientListener();
        mChatManager.registerListener(mClientListener);

        Intent intent = getIntent();
        mIsPeerToPeerMode = intent.getBooleanExtra(MessageUtil.INTENT_EXTRA_IS_PEER_MODE, true);
        mUserId = intent.getStringExtra(MessageUtil.INTENT_EXTRA_USER_ID);
        String targetName = intent.getStringExtra(MessageUtil.INTENT_EXTRA_TARGET_NAME);
        getChat_from_sqlite(targetName);
        mTitleTextView = findViewById(R.id.message_title);

        Log.d("target name channel ", targetName);
        if(intent.hasExtra("istext")){
            isTextChat = true;
            Log.d("target text chat ", true + "");
        }


        if (mIsPeerToPeerMode) {
            mPeerId = targetName;
            mTitleTextView.setText(mPeerId);

            // load history chat records
            MessageListBean messageListBean = MessageUtil.getExistMessageListBean(mPeerId);
            if (messageListBean != null) {
                mMessageBeanList.addAll(messageListBean.getMessageBeanList());
            }

            // load offline messages since last chat with this peer.
            // Then clear cached offline messages from message pool
            // since they are already consumed.
            MessageListBean offlineMessageBean = new MessageListBean(mPeerId, mChatManager);
            mMessageBeanList.addAll(offlineMessageBean.getMessageBeanList());
            mChatManager.removeAllOfflineMessages(mPeerId);
        } else {
            mChannelName = targetName;
            mChannelMemberCount = 1;
            mTitleTextView.setText(mChannelName + "(" + mChannelMemberCount + ")");
            createAndJoinChannel();
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mMessageAdapter = new MessageAdapter(this, mMessageBeanList);
        mRecyclerView = findViewById(R.id.message_list);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mMessageAdapter);

        mMsgEditText = findViewById(R.id.message_edittiext);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIsPeerToPeerMode) {
            MessageUtil.addMessageListBeanList(new MessageListBean(mPeerId, mMessageBeanList));
        } else {
            leaveAndReleaseChannel();
        }
        mChatManager.unregisterListener(mClientListener);
    }

    public void onClickSend(View v) {

        msg = mMsgEditText.getText().toString();
        if (mIsPeerToPeerMode) {

            if(msg.contains("maps.google.com")){
                send_live_location(msg.concat("~location"));
            }else{
                if (Constants.uris.size() > 0) {
                    loader.startSender();
                    contentsize = Constants.uris.size()-1;
                    recyclerview.setVisibility(View.VISIBLE);
                    for (int i = 0; i < Constants.uris.size(); i++) {
                        last_pos_to_Send = i;
                        BackgroundUpload backgroundUpload = new BackgroundUpload();
                        backgroundUpload.execute(Constants.uris.get(i));
                    }
                } else if (!msg.equals("")) {
                    MessageBean messageBean = new MessageBean(mUserId, msg, true,
                            DateAndTimeUtils.getInstance(MessageActivity.this).getCurrentDate(),
                            DateAndTimeUtils.getInstance(MessageActivity.this).getCurrentTime());
                    mMessageBeanList.add(messageBean);
                    mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size(), 1);
                    mRecyclerView.scrollToPosition(mMessageBeanList.size() - 1);
                    sendPeerMessage(msg);
                }
                Constants.uris.clear();
            }



        } else {
            if(msg.contains("maps.google.com")){
                send_live_location(msg.concat("~location"));
            }else {
                if (Constants.uris.size() > 0) {
                    loader.startSender();
                    contentsize = Constants.uris.size()-1;
                    recyclerview.setVisibility(View.VISIBLE);
                    for (int i = 0; i < Constants.uris.size(); i++) {
                        last_pos_to_Send = i;
                        BackgroundUpload backgroundUpload = new BackgroundUpload();
                        backgroundUpload.execute(Constants.uris.get(i));
                    }
                } else if (!msg.equals("")) {
                    MessageBean messageBean = new MessageBean(mUserId, msg, true,
                            DateAndTimeUtils.getInstance(MessageActivity.this).getCurrentDate(),
                            DateAndTimeUtils.getInstance(MessageActivity.this).getCurrentTime());
                    mMessageBeanList.add(messageBean);
                    mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size(), 1);
                    mRecyclerView.scrollToPosition(mMessageBeanList.size() - 1);
                    sendChannelMessage(msg);
                }
                Constants.uris.clear();
            }


        }


        mMsgEditText.setText("");

        /*if (message_urls.size() > 0) {
            for (int i = 0; i < message_urls.size(); i++) {
                if (!message_urls.get(i).contains("~image") && !message_urls.get(i).contains("~video")) {
                    msg = mMsgEditText.getText().toString();
                }
            }
        } else {
            if (!msg.contains("~image") && !msg.contains("~video")) {
                msg = mMsgEditText.getText().toString();
            }
        }

        if (mIsPeerToPeerMode) {

            if (message_urls.size() > 0) {
                for (int i = 0; i < message_urls.size(); i++) {
                    MessageBean messageBean = new MessageBean(mUserId, message_urls.get(i), true);
                    mMessageBeanList.add(messageBean);
                    mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size(), 1);
                    mRecyclerView.scrollToPosition(mMessageBeanList.size() - 1);
                    sendPeerMessage(message_urls.get(i));
                    Log.d("message to send ", "from method" + message_urls.get(i));
                }
            } else if (!msg.equals("")) {
                MessageBean messageBean = new MessageBean(mUserId, msg, true);
                mMessageBeanList.add(messageBean);
                mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size(), 1);
                mRecyclerView.scrollToPosition(mMessageBeanList.size() - 1);
                sendPeerMessage(msg);
            }
            Constants.images_uri.clear();
            message_urls.clear();

        } else {
            sendChannelMessage(msg);
        }

        mMsgEditText.setText("");*/
    }

    private void media_options() {

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFABOpen) {
                    showFABMenu();
                } else {
                    closeFABMenu();
                }
            }
        });

    }

    private void showFABMenu() {
        isFABOpen = true;
        fab_location.animate().translationY(-getResources().getDimension(R.dimen.standard_65));
        fab_video.animate().translationY(-getResources().getDimension(R.dimen.standard_125));
        fab_photo.animate().translationY(-getResources().getDimension(R.dimen.standard_185));

        fab_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent i = new Intent(MessageActivity.this, GalleryItemsActivity.class);
                i.putExtra("opengallery", true);
                startActivity(i);*/
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select"), 21);

            }
        });

        fab_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), 22);
            }
        });

        fab_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermissions()){
                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MessageActivity.this);
                    getLastLocation();
                }else{
                    requestPermissions();
                }
            }
        });

        rotateFabForward();
    }

    private void closeFABMenu() {
        isFABOpen = false;
        fab_location.animate().translationY(0);
        fab_video.animate().translationY(0);
        fab_photo.animate().translationY(0);
        rotateFabBackward();
    }

    public void rotateFabForward() {
        ViewCompat.animate(fab).rotation(45.0F).withLayer().setDuration(300L).setInterpolator(new OvershootInterpolator(5.0F)).start();
    }

    public void rotateFabBackward() {
        ViewCompat.animate(fab).rotation(0.0F).withLayer().setDuration(300L).setInterpolator(new OvershootInterpolator(5.0F)).start();
    }

    public void onClickFinish(View v) {

        //finish();

        /*try {
            mRtmClient.logout(null);
            MessageUtil.cleanMessageListBeanList();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }

    /**
     * API CALL: send message to peer
     */
    private void sendPeerMessage(final String content) {
        recyclerview.setVisibility(View.GONE);
        // step 1: create a message
        final RtmMessage message = mRtmClient.createMessage();

        message.setText(content);
        Log.d("content to send ", "from sendPeerMessage is " + content);

        // step 2: send message to peer
        mRtmClient.sendMessageToPeer(mPeerId, message, mChatManager.getSendMessageOptions(), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                saveChat_in_sqlite(content, mPeerId, true, true);

            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                // refer to RtmStatusCode.PeerMessageState for the message state
                final int errorCode = errorInfo.getErrorCode();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (errorCode) {
                            case RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_TIMEOUT:
                            case RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_FAILURE:
                                showToast(getString(R.string.send_msg_failed));
                                break;
                            case RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_PEER_UNREACHABLE:
                                //showToast(getString(R.string.peer_offline));
                                break;
                            case RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_CACHED_BY_SERVER:
                                //showToast(getString(R.string.message_cached));
                                saveChat_in_sqlite(content, mPeerId, true, true);
                                break;

                        }
                    }
                });
            }
        });
    }

    private void saveChat_in_sqlite(String content, String Id, boolean beSelf, boolean isPeer) {
        try {
            JSONObject messagebean = new JSONObject();
            messagebean.put("account", Id);
            messagebean.put("message", content);
            messagebean.put("beSelf", beSelf);
            ContentValues c = new ContentValues();
            if(isPeer){
                c.put(DatabaseHelper.JSON_CHAT, messagebean.toString());
                c.put(DatabaseHelper.ROOM_NAME, String.valueOf(Id)); //peerId
                c.put(DatabaseHelper.CHAT_DATE, DateAndTimeUtils.getInstance(MessageActivity.this).getCurrentDate());
                c.put(DatabaseHelper.CHAT_TIME, DateAndTimeUtils.getInstance(MessageActivity.this).getCurrentTime());
            }else{
                c.put(DatabaseHelper.JSON_CHAT, messagebean.toString());
                c.put(DatabaseHelper.ROOM_NAME, String.valueOf(mChannelName));
                c.put(DatabaseHelper.CHAT_DATE, DateAndTimeUtils.getInstance(MessageActivity.this).getCurrentDate());
                c.put(DatabaseHelper.CHAT_TIME, DateAndTimeUtils.getInstance(MessageActivity.this).getCurrentTime());
            }
            DatabaseHelper.getInstance(MessageActivity.this).save_TABLE_CHAT(c, mPeerId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getChat_from_sqlite(String roomname) {
        try {

            if(!roomname.trim().equals("")){
                JSONArray array_chat = DatabaseHelper.getInstance(MessageActivity.this).get_TABLE_CHAT(Integer.parseInt(roomname));

                Log.d("array ", array_chat + "");

                if (array_chat != null && array_chat.length() > 0) {
                    for (int i = 0; i < array_chat.length(); i++) {
                        JSONObject obj = new JSONObject(array_chat.optString(i));
                        mMessageBeanList.add(new MessageBean(obj.getString("account"), obj.getString("message"),
                                obj.optBoolean("beSelf"), obj.getString("date"),obj.getString("time")));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * API CALL: create and join channel
     */
    private void createAndJoinChannel() {
        // step 1: create a channel instance
        mRtmChannel = mRtmClient.createChannel(mChannelName, new MyChannelListener());
        if (mRtmChannel == null) {
            //showToast(getString(R.string.join_channel_failed));
            finish();
            return;
        }

        //Log.e("channel", mRtmChannel + "");

        // step 2: join the channel
        mRtmChannel.join(new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                Log.i(TAG, "join channel success");
                getChannelMemberList();
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                Log.e(TAG, "join channel failed " + errorInfo.getErrorDescription());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //showToast(getString(R.string.join_channel_failed));
                        //finish();
                    }
                });
            }
        });
    }

    /**
     * API CALL: get channel member list
     */
    private void getChannelMemberList() {
        mRtmChannel.getMembers(new ResultCallback<List<RtmChannelMember>>() {
            @Override
            public void onSuccess(final List<RtmChannelMember> responseInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChannelMemberCount = responseInfo.size();
                        refreshChannelTitle();
                        //Log.d("message details size ", "\nNumber of members  " + mChannelMemberCount);
                    }
                });
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                Log.e(TAG, "failed to get channel members, err: " + errorInfo.getErrorCode());
            }
        });
    }

    /**
     * API CALL: send message to a channel
     */
    private void sendChannelMessage(final String content) {
        recyclerview.setVisibility(View.GONE);
        // step 1: create a message
        RtmMessage message = mRtmClient.createMessage();
        message.setText(content);

        Log.d("channel message ", content);

        //Log.e("channel", mRtmChannel + "");

        // step 2: send message to channel
        mRtmChannel.sendMessage(message, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                saveChat_in_sqlite(content, mUserId, true, false);
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                // refer to RtmStatusCode.ChannelMessageState for the message state
                final int errorCode = errorInfo.getErrorCode();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (errorCode) {
                            case RtmStatusCode.ChannelMessageError.CHANNEL_MESSAGE_ERR_TIMEOUT:
                            case RtmStatusCode.ChannelMessageError.CHANNEL_MESSAGE_ERR_FAILURE:
                                showToast(getString(R.string.send_msg_failed));
                                break;
                        }
                    }
                });
            }
        });
    }

    /**
     * API CALL: leave and release channel
     */
    private void leaveAndReleaseChannel() {
        if (mRtmChannel != null) {
            mRtmChannel.leave(null);
            mRtmChannel.release();
            mRtmChannel = null;
        }
    }

    public void goto_showmedia_fragment(String url, String type) {
        Intent i = new Intent(MessageActivity.this, ShowMediaActivity.class);

        if (type.equalsIgnoreCase("image"))
            i.putExtra("urlimage", url);
        else
            i.putExtra("urlvideo", url);
        //Log.d("url to open ", url);
        startActivity(i);
    }

    /**
     * API CALLBACK: rtm event listener
     */
    class MyRtmClientListener implements RtmClientListener {

        @Override
        public void onConnectionStateChanged(final int state, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (state) {
                        case RtmStatusCode.ConnectionState.CONNECTION_STATE_RECONNECTING:
                            showToast(getString(R.string.reconnecting));
                            break;
                        case RtmStatusCode.ConnectionState.CONNECTION_STATE_ABORTED:
                            showToast(getString(R.string.account_offline));
                            setResult(MessageUtil.ACTIVITY_RESULT_CONN_ABORTED);
                            finish();
                            break;
                    }
                }
            });
        }

        @Override
        public void onMessageReceived(final RtmMessage message, final String peerId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String content = message.getText();
                    Log.d("message recvd ", "in onMessageReceives is " + content);
                    if (peerId.equals(mPeerId)) {
                        MessageBean messageBean = new MessageBean(peerId, content, false,
                                DateAndTimeUtils.getInstance(MessageActivity.this).getCurrentDate(),
                                DateAndTimeUtils.getInstance(MessageActivity.this).getCurrentTime());
                        messageBean.setBackground(getMessageColor(peerId));
                        mMessageBeanList.add(messageBean);
                        mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size(), 1);
                        mRecyclerView.scrollToPosition(mMessageBeanList.size() - 1);
                    } else {
                        MessageUtil.addMessageBean(peerId, content);
                    }

                    saveChat_in_sqlite(content, peerId, false, true);

                    /*Log.d("message details recv ", "\npeer id " + peerId + "\nmessage " + message +
                            "\ncontent " + content);*/
                }
            });
        }

        @Override
        public void onTokenExpired() {

        }

        /*@Override
        public void onPeersOnlineStatusChanged(Map<String, Integer> map) {

        }*/
    }

    /**
     * API CALLBACK: rtm channel event listener
     */
    class MyChannelListener implements RtmChannelListener {
        @Override
        public void onMemberCountUpdated(int i) {

        }

        @Override
        public void onAttributesUpdated(List<RtmChannelAttribute> list) {

        }

        @Override
        public void onMessageReceived(final RtmMessage message, final RtmChannelMember fromMember) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String account = fromMember.getUserId();
                    String msg = message.getText();

                    saveChat_in_sqlite(msg, account, false, false);

                    Log.i(TAG, "onMessageReceived account = " + account + " msg = " + msg);
                    MessageBean messageBean = new MessageBean(account, msg, false,
                            DateAndTimeUtils.getInstance(MessageActivity.this).getCurrentDate(),
                            DateAndTimeUtils.getInstance(MessageActivity.this).getCurrentTime());
                    messageBean.setBackground(getMessageColor(account));
                    mMessageBeanList.add(messageBean);
                    mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size(), 1);
                    mRecyclerView.scrollToPosition(mMessageBeanList.size() - 1);
                }
            });
        }

        @Override
        public void onMemberJoined(RtmChannelMember member) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mChannelMemberCount++;
                    refreshChannelTitle();
                }
            });
        }

        @Override
        public void onMemberLeft(RtmChannelMember member) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mChannelMemberCount--;
                    refreshChannelTitle();
                }
            });
        }
    }

    private int getMessageColor(String account) {
        for (int i = 0; i < mMessageBeanList.size(); i++) {
            if (account.equals(mMessageBeanList.get(i).getAccount())) {
                return mMessageBeanList.get(i).getBackground();
            }
        }
        return MessageUtil.COLOR_ARRAY[MessageUtil.RANDOM.nextInt(MessageUtil.COLOR_ARRAY.length)];
    }

    private void refreshChannelTitle() {
        String titleFormat = getString(R.string.channel_title);
        String title = String.format(titleFormat, mChannelName, mChannelMemberCount);
        mTitleTextView.setText(title);
    }

    private void showToast(final String text) {
        Toast.makeText(MessageActivity.this, text, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (resultCode == RESULT_OK) {
                // When an Image is picked
                if (requestCode == 21 && null != data) {
                    upload_type = "image";

                    // Get the Image from data

                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    imagesEncodedList = new ArrayList<String>();
                    if (data.getData() != null) {

                        Uri mImageUri = data.getData();

                        // Get the cursor
                        Cursor cursor = getContentResolver().query(mImageUri,
                                filePathColumn, null, null,
                                null);
                        // Move to first row
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        imageEncoded = cursor.getString(columnIndex);
                        cursor.close();

                        Constants.uris.add(mImageUri);
                        galleryAdapter = new GalleryAdapter(getApplicationContext(), Constants.uris);
                        recyclerview.setAdapter(galleryAdapter);

                        Toast toast = Toast.makeText(this, "Your image has been selected", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                    } else if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        //ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            Constants.uris.add(uri);
                            // Get the cursor
                            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                            // Move to first row
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageEncoded = cursor.getString(columnIndex);
                            imagesEncodedList.add(imageEncoded);
                            cursor.close();

                            galleryAdapter = new GalleryAdapter(getApplicationContext(), Constants.uris);
                            recyclerview.setAdapter(galleryAdapter);

                            Toast toast = Toast.makeText(this, "Your images have been selected", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();

                        }

                        Log.v("LOG_TAG", "Selected Images" + Constants.uris.size());
                    }
                } else if (requestCode == 22 && data.getData() != null) {
                    upload_type = "video";

                    Uri selectedVideoUri = data.getData();
                    String vidPath = "";
                    // OI FILE Manager
                    String filemanagerstring = selectedVideoUri.toString();
                    vidPath = filemanagerstring;

                    // MEDIA GALLERY
                    String selectedVideoPath = getPath(selectedVideoUri);
                    if (selectedVideoPath != null) {
                        vidPath = selectedVideoPath;
                    }
                    Log.d("video path ", vidPath);
                    Constants.uris.add(selectedVideoUri);

                    Toast toast = Toast.makeText(this, "Your video has been selected", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    //uploadDatatoFirestore(selectedVideoUri, "video");
                }
            }

        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

        /**/
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;

    }

    private void uploadDatatoFirestore(final Uri filePath, final String type) {
        if (filePath != null && preferences != null) {
            final StorageReference ref = storageReference.child(String.valueOf(mUserId)).child(UUID.randomUUID().toString());
            /*if(friendname==0){
                ref = storageReference.child(String.valueOf(accountname)).child(UUID.randomUUID().toString());
            }else{
                ref = storageReference.child(accountname + "-" + friendname).child(UUID.randomUUID().toString());
            }*/

            ref.putFile(filePath).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //Toast.makeText(MessageActivity.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();

                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    if (type.equalsIgnoreCase("image")) {
                                        Log.d("url download is ", uri.toString() + "~image");

                                        MessageBean messageBean = new MessageBean(mUserId, (uri.toString() + "~image"), true,
                                                DateAndTimeUtils.getInstance(MessageActivity.this).getCurrentDate(),
                                                DateAndTimeUtils.getInstance(MessageActivity.this).getCurrentTime());
                                        mMessageBeanList.add(messageBean);
                                        mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size(), 1);
                                        mRecyclerView.scrollToPosition(mMessageBeanList.size() - 1);

                                        if (mIsPeerToPeerMode) {
                                            sendPeerMessage((uri.toString() + "~image"));
                                        } else {
                                            sendChannelMessage((uri.toString() + "~image"));
                                        }

                                        mMsgEditText.setText("");

                                    } else if (type.equalsIgnoreCase("video")) {
                                        Log.d("url download is ", uri.toString() + "~video");

                                        MessageBean messageBean = new MessageBean(mUserId, (uri.toString() + "~video"), true,
                                                DateAndTimeUtils.getInstance(MessageActivity.this).getCurrentDate(),
                                                DateAndTimeUtils.getInstance(MessageActivity.this).getCurrentTime());
                                        mMessageBeanList.add(messageBean);
                                        mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size(), 1);
                                        mRecyclerView.scrollToPosition(mMessageBeanList.size() - 1);

                                        if (mIsPeerToPeerMode) {
                                            sendPeerMessage((uri.toString() + "~video"));
                                        } else {
                                            sendChannelMessage((uri.toString() + "~video"));
                                        }

                                    }

                                    dismissloader();

                                }
                            });

                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("failed ", "uploading on server ");
                            //Toast.makeText(MessageActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            dismissloader();
                        }
                    });
        }
    }

    private void dismissloader() {
        try{
            Log.d("size to check ",last_pos_to_Send +"    " + contentsize);
            if(last_pos_to_Send==contentsize){
                loader.dismissLoader();
            }
        }catch (Exception e){
            loader.dismissLoader();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private class BackgroundUpload extends AsyncTask<Uri, Void, Void> {

        @Override
        protected Void doInBackground(Uri... voids) {
            uploadDatatoFirestore(voids[0], upload_type);
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        try{
        /*try {
            mRtmClient.logout(null);
            MessageUtil.cleanMessageListBeanList();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        if (mMessageBeanList != null) {
            mMessageBeanList.clear();
        }

        if(isTextChat){
            if (preferences != null && preferences.contains("loginpatient")) {
                Intent i = new Intent(MessageActivity.this, PatientViewActivity.class);
                startActivity(i);
                finish();
            } else if (preferences != null && preferences.contains("logindoctor")) {
                Intent i = new Intent(MessageActivity.this, DoctorViewActivity.class);
                startActivity(i);
                finish();
            } else {
                Intent i = new Intent(MessageActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        } else if (mIsPeerToPeerMode) {
            if (preferences != null && preferences.contains("loginpatient")) {
                Intent i = new Intent(MessageActivity.this, MainActivity.class);
                i.putExtra("channelname", densoftinfotechio.videocall.openlive.Constants.channel);
                startActivity(i);
                finish();
            } else if (preferences != null && preferences.contains("logindoctor")) {
                Intent i = new Intent(MessageActivity.this, MainActivity.class);
                Log.d("target name intent ", mChannelName);
                i.putExtra("channelname", densoftinfotechio.videocall.openlive.Constants.channel);
                startActivity(i);
                finish();
            } else {
                Intent i = new Intent(MessageActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        } else {
            if (preferences != null && preferences.contains("loginpatient")) {
                Intent i = new Intent(MessageActivity.this, MainActivity.class);
                Log.d("channelname ", mChannelName);
                i.putExtra("channelname", Integer.parseInt(mChannelName));
                i.putExtra("type", densoftinfotechio.videocall.openlive.Constants.type);
                startActivity(i);
                finish();
            } else if (preferences != null && preferences.contains("logindoctor")) {
                Log.d("channelname ", mChannelName);
                Intent i = new Intent(MessageActivity.this, MainActivity.class);
                i.putExtra("channelname", Integer.parseInt(mChannelName));
                i.putExtra("type", "Host");
                startActivity(i);
                finish();
            } else {
                Intent i = new Intent(MessageActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        }
    }catch(Exception e){
            e.printStackTrace();
        }

    }

    private boolean checkPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=loc:%f,%f",
                    mLastLocation.getLatitude(),mLastLocation.getLongitude());
            mMsgEditText.setText("I'm here " + uri);
            //latTextView.setText(mLastLocation.getLatitude()+"");
            //lonTextView.setText(mLastLocation.getLongitude()+"");
        }
    };

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }


    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=loc:%f,%f",
                                            location.getLatitude(),location.getLongitude());
                                    mMsgEditText.setText("I'm here " + uri);

                                    //latTextView.setText(location.getLatitude()+"");
                                    //lonTextView.setText(location.getLongitude()+"");
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    private void send_live_location(String uri) {
        MessageBean messageBean = new MessageBean(mUserId, (uri), true,
                DateAndTimeUtils.getInstance(MessageActivity.this).getCurrentDate(),
                DateAndTimeUtils.getInstance(MessageActivity.this).getCurrentTime());
        mMessageBeanList.add(messageBean);
        mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size(), 1);
        mRecyclerView.scrollToPosition(mMessageBeanList.size() - 1);

        if (mIsPeerToPeerMode) {
            sendPeerMessage((uri));
        } else {
            sendChannelMessage((uri));
        }

        mMsgEditText.setText("");
    }
}
