package cl.sebastian.miscalorias;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Ajustes (meta diaria y hora de corte) y utilidades de fecha y números. */
public final class Prefs {

    public static final Locale CL = Locale.forLanguageTag("es-CL");

    private Prefs() { }

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences("ajustes", Context.MODE_PRIVATE);
    }

    public static int goal(Context c) {
        return sp(c).getInt("meta", 1500);
    }

    public static void setGoal(Context c, int v) {
        sp(c).edit().putInt("meta", Math.max(1, v)).apply();
    }

    /** Hasta esta hora de la madrugada, lo que comes cuenta para el día anterior. */
    public static int cutoff(Context c) {
        return sp(c).getInt("corte", 4);
    }

    public static void setCutoff(Context c, int v) {
        sp(c).edit().putInt("corte", v).apply();
    }

    public static Calendar dayCal(Context c, int offset) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -cutoff(c));
        cal.add(Calendar.DAY_OF_MONTH, offset);
        return cal;
    }

    public static String dayKey(Calendar cal) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
    }

    public static String today(Context c) {
        return dayKey(dayCal(c, 0));
    }

    public static String fmt(int n) {
        return String.format(CL, "%,d", n);
    }

    public static String pretty(String dayKey, String pattern) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dayKey);
            return new SimpleDateFormat(pattern, CL).format(d);
        } catch (ParseException e) {
            return dayKey;
        }
    }

    public static String cap(String s) {
        return s.isEmpty() ? s : s.substring(0, 1).toUpperCase(CL) + s.substring(1);
    }
}
