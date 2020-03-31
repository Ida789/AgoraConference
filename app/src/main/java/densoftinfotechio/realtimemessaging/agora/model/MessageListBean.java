package densoftinfotechio.realtimemessaging.agora.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import densoftinfotechio.classes.DateAndTimeUtils;
import densoftinfotechio.realtimemessaging.agora.activity.MessageActivity;
import densoftinfotechio.realtimemessaging.agora.rtmtutorial.ChatManager;
import io.agora.rtm.RtmMessage;

public class MessageListBean {
    private String accountOther;
    private List<MessageBean> messageBeanList;
    private SimpleDateFormat sdf_date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private Calendar c;

    public MessageListBean(String account, List<MessageBean> messageBeanList) {
        this.accountOther = account;
        this.messageBeanList = messageBeanList;
    }

    /**
     * Create message list bean from offline messages
     * @param account peer user id to find offline messages from
     * @param chatManager chat manager that managers offline message pool
     */
    public MessageListBean(String account, ChatManager chatManager) {
        accountOther = account;
        messageBeanList = new ArrayList<>();

        List<RtmMessage> messageList = chatManager.getAllOfflineMessages(account);
        for (RtmMessage m : messageList) {
            // All offline messages are from peer users
            MessageBean bean = new MessageBean(account, m.getText(), false, getCurrentDate(), getCurrentTime());
            messageBeanList.add(bean);
        }
    }

    public String getAccountOther() {
        return accountOther;
    }

    public void setAccountOther(String accountOther) {
        this.accountOther = accountOther;
    }

    public List<MessageBean> getMessageBeanList() {
        return messageBeanList;
    }

    public void setMessageBeanList(List<MessageBean> messageBeanList) {
        this.messageBeanList = messageBeanList;
    }

    public String getCurrentDate(){
        c = Calendar.getInstance();
        return sdf_date.format(c.getTime());
    }

    public String getCurrentTime(){
        c = Calendar.getInstance();
        return sdf_time.format(c.getTime());
    }
}
