package densoftinfotechio.realtimemessaging.agora.model;

public class MessageBean {
    private String account;
    private String message;
    private int background;
    private boolean beSelf;
    private String date;
    private String time;

    /*public MessageBean(String account, String message, boolean beSelf) {
        this.account = account;
        this.message = message;
        this.beSelf = beSelf;
    }*/
    public MessageBean(String account, String message, boolean beSelf, String date, String time) {
        this.account = account;
        this.message = message;
        this.beSelf = beSelf;
        this.date = date;
        this.time = time;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isBeSelf() {
        return beSelf;
    }

    public void setBeSelf(boolean beSelf) {
        this.beSelf = beSelf;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
