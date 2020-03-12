package densoftinfotechio.classes;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateAndTimeUtils {
    private String date = "";
    private String time = "";
    private SimpleDateFormat sdf_date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private Calendar c;
    private static Context context;
    private static DateAndTimeUtils instance = null;

    public DateAndTimeUtils(Context context){
        this.context = context;
    }

    public static synchronized DateAndTimeUtils getInstance(Context context1){
        context = context1;
        if(instance == null){
            instance = new DateAndTimeUtils(context);
        }

        return instance;
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
