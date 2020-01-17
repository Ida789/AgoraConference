// IScreenSharing.aidl
package densoftinfotechio.agora.rtc.ss.aidl;

import densoftinfotechio.agora.rtc.ss.aidl.INotification;

// Declare any non-default types here with import statements

interface IScreenSharing {
    void registerCallback(INotification callback);
    void unregisterCallback(INotification callback);
    void startShare();
    void stopShare();
    void renewToken(String token);
}
