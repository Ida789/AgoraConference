package densoftinfotechio.realtimemessaging.agora.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import densoftinfotechio.AgoraApplication;
import densoftinfotechio.agora.openlive.R;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmClient;
//import densoftinfotechio.realtimemessaging.agora.rtmtutorial.AGApplication;
import densoftinfotechio.realtimemessaging.agora.rtmtutorial.ChatManager;
import densoftinfotechio.realtimemessaging.agora.utils.MessageUtil;


public class LoginActivity extends Activity {
    private final String TAG = LoginActivity.class.getSimpleName();

    private TextView mLoginBtn;
    private EditText mUserIdEditText;
    private String mUserId;

    private ChatManager mChatManager;
    private RtmClient mRtmClient;
    private boolean mIsInChat = false;
    Bundle b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agora_rtm_activity_login);

        mUserIdEditText = findViewById(R.id.user_id);
        mLoginBtn = findViewById(R.id.button_login);

        mChatManager = AgoraApplication.the().getChatManager();
        mRtmClient = mChatManager.getRtmClient();

        b = getIntent().getExtras();
        onLogin();


    }

    public void onClickLogin(View v) {
        onLogin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLoginBtn.setEnabled(true);
        if (mIsInChat) {
            doLogout();
        }
    }

    /**
     * API CALL: login RTM server
     */
    private void doLogin(final int friendname, final int accountname) {
        mIsInChat = true;
        mRtmClient.login(null, mUserId, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                Log.i(TAG, "login success");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(LoginActivity.this, SelectionActivity.class);
                        intent.putExtra(MessageUtil.INTENT_EXTRA_USER_ID, mUserId);
                        intent.putExtra("friendname", friendname);
                        intent.putExtra("accountname", accountname);
                        startActivity(intent);
                        finish();
                    }
                });
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                Log.i(TAG, "login failed: " + errorInfo.getErrorCode());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoginBtn.setEnabled(true);
                        mIsInChat = false;
                        showToast(getString(R.string.login_failed));
                    }
                });
            }
        });
    }

    /**
     * API CALL: logout from RTM server
     */
    private void doLogout() {
        mRtmClient.logout(null);
        MessageUtil.cleanMessageListBeanList();
    }

    private void onLogin(){

        if(b!=null && b.containsKey("accountname") && b.containsKey("friendname")){
            mUserIdEditText.setText(String.valueOf(b.getInt("accountname",0)));
            mUserId = mUserIdEditText.getText().toString();
            Log.d("here flow ", "part 4 LoginActivity " + " user id " + b.getInt("accountname",0));
            if (mUserId.equals("")) {
                showToast(getString(R.string.account_empty));
            } else if (mUserId.length() > MessageUtil.MAX_INPUT_NAME_LENGTH) {
                showToast(getString(R.string.account_too_long));
            } else if (mUserId.startsWith(" ")) {
                showToast(getString(R.string.account_starts_with_space));
            } else if (mUserId.equals("null")) {
                showToast(getString(R.string.account_literal_null));
            } else {
                mLoginBtn.setEnabled(false);
                doLogin(b.getInt("friendname",0), b.getInt("accountname",0));
            }
        }
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
