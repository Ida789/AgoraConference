package densoftinfotechio.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import densoftinfotechio.agora.openlive.R;

public class Loader {
    Activity activity;
    AlertDialog dialog;

    public Loader(Activity activity) {
        this.activity = activity;
    }

    public void startLoader() {
        try {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            alertBuilder.setView(inflater.inflate(R.layout.loader_dialog, null));
            alertBuilder.setCancelable(false);
            dialog = alertBuilder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissLoader() {
        try {
            if (!activity.isFinishing() && dialog != null) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
