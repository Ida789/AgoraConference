package densoftinfotechio.realtimemessaging.agora.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import densoftinfotechio.AgoraApplication;
import densoftinfotechio.agora.openlive.R;
import densoftinfotechio.realtimemessaging.agora.rtmtutorial.ChatManager;
import densoftinfotechio.realtimemessaging.agora.utils.MessageUtil;
import io.agora.rtm.RtmClient;


public class SelectionActivity extends Activity {
    private final String TAG = SelectionActivity.class.getSimpleName();

    private static final int CHAT_REQUEST_CODE = 1;

    private TextView mTitleTextView;
    private TextView mChatButton;
    private EditText mNameEditText;
    private CheckBox mOfflineMsgCheck;

    private boolean mIsPeerToPeerMode = true; // whether peer to peer mode or channel mode\
    private String mTargetName;
    private String mUserId;

    private ChatManager mChatManager;
    private RtmClient mRtmClient;
    private Bundle b;
    //int accountname = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agora_rtm_activity_selection);

        mChatManager = AgoraApplication.the().getChatManager();
        mRtmClient = mChatManager.getRtmClient();

        initUIAndData();
    }

    private void initUIAndData() {
        Intent intent = getIntent();
        mUserId = intent.getStringExtra(MessageUtil.INTENT_EXTRA_USER_ID);
        mTitleTextView = findViewById(R.id.selection_title);
        mNameEditText = findViewById(R.id.selection_name);
        mChatButton = findViewById(R.id.selection_chat_btn);
        RadioGroup modeGroup = findViewById(R.id.mode_radio_group);

        modeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.peer_radio_button:
                        mIsPeerToPeerMode = true;
                        mTitleTextView.setText(getString(R.string.title_peer_msg));
                        mChatButton.setText(getString(R.string.btn_chat));
                        //mNameEditText.setHint(getString(R.string.hint_friend));
                        break;
                    case R.id.selection_tab_channel:
                        mIsPeerToPeerMode = false;
                        mTitleTextView.setText(getString(R.string.title_channel_message));
                        mChatButton.setText(getString(R.string.btn_join));
                        //mNameEditText.setHint(getString(R.string.hint_channel));
                        break;
                }
            }
        });

        mOfflineMsgCheck = findViewById(R.id.offline_msg_check);
        mOfflineMsgCheck.setChecked(true);
        mChatManager.enableOfflineMessage(true);

        b = getIntent().getExtras();
        if (b != null) {

            if (b.containsKey("friendname") && b.containsKey("accountname")) {
                mNameEditText.setText(String.valueOf(b.getInt("friendname")));
                //accountname = b.getInt("accountname", 0);
                RadioButton peerMode = findViewById(R.id.peer_radio_button);
                peerMode.setChecked(true);
                modeGroup.check(R.id.peer_radio_button);
            } else if (b.containsKey("channel")) {
                mNameEditText.setText(String.valueOf(b.getInt("channel", 0)));
                //accountname = b.getInt("accountname", 0);
                //Log.d("login success ", mNameEditText.getText().toString() + " account " + accountname);
                RadioButton channelMode = findViewById(R.id.selection_tab_channel);
                channelMode.setChecked(true);
                modeGroup.check(R.id.selection_tab_channel);
            }

            goto_message_actvity();

        }

        /*mOfflineMsgCheck.setChecked(mChatManager.isOfflineMessageEnabled());
        mOfflineMsgCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mChatManager.enableOfflineMessage(isChecked);
            }
        });*/
    }

    public void onClickChat(View v) {
        //goto_message_actvity();
    }

    private void goto_message_actvity() {
        mTargetName = mNameEditText.getText().toString();
        Log.d("login selection ", mTargetName);
        if (mTargetName.equals("")) {
            showToast(getString(mIsPeerToPeerMode ? R.string.account_empty : R.string.channel_name_empty));
        } else if (mTargetName.length() >= MessageUtil.MAX_INPUT_NAME_LENGTH) {
            showToast(getString(mIsPeerToPeerMode ? R.string.account_too_long : R.string.channel_name_too_long));
        } else if (mTargetName.startsWith(" ")) {
            showToast(getString(mIsPeerToPeerMode ? R.string.account_starts_with_space : R.string.channel_name_starts_with_space));
        } else if (mTargetName.equals("null")) {
            showToast(getString(mIsPeerToPeerMode ? R.string.account_literal_null : R.string.channel_name_literal_null));
        } else if (mIsPeerToPeerMode && mTargetName.equals(mUserId)) {
            showToast(getString(R.string.account_cannot_be_yourself));
        } else {
            mChatButton.setEnabled(false);
            jumpToMessageActivity();
        }
    }

    private void jumpToMessageActivity() {
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra(MessageUtil.INTENT_EXTRA_IS_PEER_MODE, mIsPeerToPeerMode);
        intent.putExtra(MessageUtil.INTENT_EXTRA_TARGET_NAME, mTargetName);
        intent.putExtra(MessageUtil.INTENT_EXTRA_USER_ID, mUserId);
        if (b.containsKey("istext")) {
            intent.putExtra("istext", true);
        }
        startActivityForResult(intent, CHAT_REQUEST_CODE);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mChatButton.setEnabled(true);
    }

    public void onClickFinish(View v) {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHAT_REQUEST_CODE) {
            if (resultCode == MessageUtil.ACTIVITY_RESULT_CONN_ABORTED) {
                finish();
            }
        }
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
