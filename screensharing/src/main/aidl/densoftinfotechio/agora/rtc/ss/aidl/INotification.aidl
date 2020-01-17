// INotification.aidl
package densoftinfotechio.agora.rtc.ss.aidl;

// Declare any non-default types here with import statements

interface INotification {
    void onError(int error);
    void onTokenWillExpire();
}
