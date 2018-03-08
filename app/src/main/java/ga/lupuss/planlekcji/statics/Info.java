package ga.lupuss.planlekcji.statics;

import android.os.Build;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Field;

import ga.lupuss.planlekcji.BuildConfig;

public final  class Info {

    public final static String API_URL = "url to API here";
    private final static char[] apiAuth = "login:password".toCharArray();
    public static File APP_FILES_DIR;

    public static String getAuthorization(){

        byte[] pass = new String(apiAuth).getBytes();

        return "Basic " + Base64.encodeToString(pass, Base64.DEFAULT);
    }

    public static String getUserAgent() {

        return "Aplikacja plan lekcji " + BuildConfig.VERSION_NAME;
    }

    public static String getDeviceName() {

        String manuf = Build.MANUFACTURER;
        String model = Build.MODEL;

        if (model.startsWith(manuf)) {

            return model;
        }
        return manuf + " " + model;
    }

    public static String getAndroidName() {

        Field[] fields = Build.VERSION_CODES.class.getFields();
        String osName = fields[Build.VERSION.SDK_INT].getName();

        Log.d(Info.class.getName(), "Android " + osName + " " + Build.VERSION.RELEASE );

        return String.format("Android %s %s", Build.VERSION.RELEASE, osName);

    }

}
