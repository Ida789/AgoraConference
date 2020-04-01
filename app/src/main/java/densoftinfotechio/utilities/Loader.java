package densoftinfotechio.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

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

    public void startSender() {
        try {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
            View view = activity.getLayoutInflater().inflate(R.layout.loader_dialog, null);
            alertBuilder.setView(view);
            TextView tv_loading = view.findViewById(R.id.tv_loading);
            tv_loading.setText("Sending... Please Wait...");
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
