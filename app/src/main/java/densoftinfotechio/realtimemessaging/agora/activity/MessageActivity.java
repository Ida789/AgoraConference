package densoftinfotechio.realtimemessaging.agora.activity;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import densoftinfotechio.AgoraApplication;
import densoftinfotechio.GalleryItemsActivity;
import densoftinfotechio.ShowMediaActivity;
import densoftinfotechio.adapter.GalleryAdapter;
import densoftinfotechio.agora.openlive.R;
import densoftinfotechio.classes.Constants;
import densoftinfotechio.realtimemessaging.agora.adapter.MessageAdapter;
import densoftinfotechio.realtimemessaging.agora.model.MessageBean;
import densoftinfotechio.realtimemessaging.agora.model.MessageListBean;
import densoftinfotechio.realtimemessaging.agora.rtmtutorial.ChatManager;
import densoftinfotechio.realtimemessaging.agora.utils.MessageUtil;
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

//import densoftinfotechio.realtimemessaging.agora.rtmtutorial.AGApplication;

//import io.agora.rtm.jni.PEER_ONLINE_STATE;
//import io.agora.rtm.jni.PeerOnlineStatus;


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
    FloatingActionButton fab, fab1, fab2, fab3;
    private StorageReference storageReference;
    private FirebaseStorage firebaseStorage;
    private Uri filePath;
    private SharedPreferences preferences;
    Bundle b;
    String accountname = "", friendname = "";
    String msg = "";
    ArrayList<String> message_urls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agora_rtm_activity_message);
        init();
    }

    private void init() {

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab3 = (FloatingActionButton) findViewById(R.id.fab3);

        media_options();

        preferences = PreferenceManager.getDefaultSharedPreferences(MessageActivity.this);
        b = getIntent().getExtras();
        if (b != null && b.containsKey("accountname") && b.containsKey("friendname")) {
            accountname = b.getString("accountname");
            friendname = b.getString("friendname");
        }
        //firebaseStorage = FirebaseStorage.getInstance("gs://videoconferencedemo.appspot.com");
        storageReference = FirebaseStorage.getInstance().getReference(Constants.firebasestoragename);

        mChatManager = AgoraApplication.the().getChatManager();
        mRtmClient = mChatManager.getRtmClient();
        mClientListener = new MyRtmClientListener();
        mChatManager.registerListener(mClientListener);

        Intent intent = getIntent();
        mIsPeerToPeerMode = intent.getBooleanExtra(MessageUtil.INTENT_EXTRA_IS_PEER_MODE, true);
        mUserId = intent.getStringExtra(MessageUtil.INTENT_EXTRA_USER_ID);
        String targetName = intent.getStringExtra(MessageUtil.INTENT_EXTRA_TARGET_NAME);

        mTitleTextView = findViewById(R.id.message_title);
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

        if(message_urls.size()>0){
            for(int i = 0; i<message_urls.size(); i++){
                if (!message_urls.get(i).contains("~image") && !message_urls.get(i).contains("~video")) {
                    msg = mMsgEditText.getText().toString();
                } else {
                    //message contains image url
                }
            }
        }else{
            if (!msg.contains("~image") && !msg.contains("~video")) {
                msg = mMsgEditText.getText().toString();
            } else {
                //message contains image url
            }
        }

        /*if (!msg.contains("~image") || !msg.contains("~video")) {
            msg = mMsgEditText.getText().toString();
        } else {
            //message contains image url
        }*/

        if (!msg.equals("")) {
            MessageBean messageBean = new MessageBean(mUserId, msg, true);
            mMessageBeanList.add(messageBean);
            mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size(), 1);
            mRecyclerView.scrollToPosition(mMessageBeanList.size() - 1);
            if (mIsPeerToPeerMode) {
                Log.d("message to send ", msg);
                if(message_urls.size()>0){
                    for(int i = 0; i<message_urls.size(); i++){
                        sendPeerMessage(message_urls.get(i));
                    }
                }else{
                    sendPeerMessage(msg);
                }


            } else {
                sendChannelMessage(msg);
            }
        }
        mMsgEditText.setText("");
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
        fab1.animate().translationY(-getResources().getDimension(R.dimen.standard_65));
        fab2.animate().translationY(-getResources().getDimension(R.dimen.standard_125));
        fab3.animate().translationY(-getResources().getDimension(R.dimen.standard_185));

        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MessageActivity.this, GalleryItemsActivity.class);
                i.putExtra("opengallery", true);
                startActivity(i);

            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), 22);
            }
        });

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        rotateFabForward();
    }

    private void closeFABMenu() {
        isFABOpen = false;
        fab1.animate().translationY(0);
        fab2.animate().translationY(0);
        fab3.animate().translationY(0);
        rotateFabBackward();
    }

    public void rotateFabForward() {
        ViewCompat.animate(fab).rotation(45.0F).withLayer().setDuration(300L).setInterpolator(new OvershootInterpolator(5.0F)).start();
    }

    public void rotateFabBackward() {
        ViewCompat.animate(fab).rotation(0.0F).withLayer().setDuration(300L).setInterpolator(new OvershootInterpolator(5.0F)).start();
    }

    public void onClickFinish(View v) {
        finish();
    }

    /**
     * API CALL: send message to peer
     */
    private void sendPeerMessage(String content) {
        // step 1: create a message
        final RtmMessage message = mRtmClient.createMessage();

        Log.d("message to method ", content);
        message.setText(content);

        // step 2: send message to peer
        mRtmClient.sendMessageToPeer(mPeerId, message, mChatManager.getSendMessageOptions(), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // do nothing
                Log.d("message to send ", message.toString());
                Log.d("message details sent ", "\npeer id " + mPeerId + "\nmessage " + message +
                        "\nChat Options " + mChatManager.getSendMessageOptions());
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
                                break;
                        }
                    }
                });
            }
        });
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

        Log.e("channel", mRtmChannel + "");

        // step 2: join the channel
        mRtmChannel.join(new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                Log.i(TAG, "join channel success");
                getChannelMemberList();
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                Log.e(TAG, "join channel failed");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(getString(R.string.join_channel_failed));
                        finish();
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
                        Log.d("message details size ", "\nNumber of members  " + mChannelMemberCount);
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
    private void sendChannelMessage(String content) {
        // step 1: create a message
        RtmMessage message = mRtmClient.createMessage();
        message.setText(content);

        Log.e("channel", mRtmChannel + "");

        // step 2: send message to channel
        mRtmChannel.sendMessage(message, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

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

    public void goto_image_fragment(String url) {
        Intent i = new Intent(MessageActivity.this, ShowMediaActivity.class);
        i.putExtra("url", url);
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
                    Log.d("message recvd ", content);
                    if (peerId.equals(mPeerId)) {
                        MessageBean messageBean = new MessageBean(peerId, content, false);
                        messageBean.setBackground(getMessageColor(peerId));
                        mMessageBeanList.add(messageBean);
                        mMessageAdapter.notifyItemRangeChanged(mMessageBeanList.size(), 1);
                        mRecyclerView.scrollToPosition(mMessageBeanList.size() - 1);
                    } else {
                        MessageUtil.addMessageBean(peerId, content);
                    }

                    Log.d("message details recv ", "\npeer id " + peerId + "\nmessage " + message +
                            "\ncontent " + content);
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
                    Log.i(TAG, "onMessageReceived account = " + account + " msg = " + msg);
                    MessageBean messageBean = new MessageBean(account, msg, false);
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

        if (data != null && data.getData() != null) {
            if (requestCode == 22 && resultCode == RESULT_OK) {
                if (data.getData() != null) {
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
                    uploadDatatoFirestore(selectedVideoUri, "video");
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to select video", Toast.LENGTH_LONG).show();
                }
            }
        }
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

            final StorageReference ref = storageReference.child(accountname + "-" + friendname).child(UUID.randomUUID().toString());

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
                                        msg = uri.toString() + "~image";
                                        message_urls.add(msg);
                                    } else if (type.equalsIgnoreCase("video")) {
                                        Log.d("url download is ", uri.toString() + "~video");
                                        msg = uri.toString() + "~video";
                                    }

                                }
                            });

                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Toast.makeText(MessageActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    /*.addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                                }
                            });*/
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    @Override
    protected void onResume() {
        if(Constants.images_uri!=null){
            Log.d("total images ",  Constants.images_uri.size() + "");
            for(int i = 0; i<Constants.images_uri.size(); i++){
                BackgroundUpload backgroundUpload = new BackgroundUpload();
                backgroundUpload.execute(Constants.images_uri.get(i));
            }
        }

        super.onResume();
    }

    private class BackgroundUpload extends AsyncTask<Uri, Void, Void>{

        @Override
        protected Void doInBackground(Uri... voids) {
            uploadDatatoFirestore(voids[0], "image");
            return null;
        }
    }

}
