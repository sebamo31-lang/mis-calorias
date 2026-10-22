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

    // ---- Tema: 0 = automático, 1 = oscuro, 2 = claro ----

    public static int theme(Context c) {
        return sp(c).getInt("tema", 1);
    }

    public static void setTheme(Context c, int v) {
        sp(c).edit().putInt("tema", v).apply();
    }

    // ---- Perfil para la calculadora ----

    public static boolean hasProfile(Context c) {
        return sp(c).contains("peso");
    }

    public static boolean askedProfile(Context c) {
        return sp(c).getBoolean("perfil_preguntado", false);
    }

    public static void setAskedProfile(Context c) {
        sp(c).edit().putBoolean("perfil_preguntado", true).apply();
    }

    public static void saveProfile(Context c, boolean mujer, int edad, float peso, int altura,
                                   int actividad, int objetivo) {
        sp(c).edit()
                .putBoolean("mujer", mujer)
                .putInt("edad", edad)
                .putFloat("peso", peso)
                .putInt("altura", altura)
                .putInt("actividad", actividad)
                .putInt("objetivo", objetivo)
                .apply();
    }

    public static boolean mujer(Context c) { return sp(c).getBoolean("mujer", false); }
    public static int edad(Context c) { return sp(c).getInt("edad", 0); }
    public static float peso(Context c) { return sp(c).getFloat("peso", 0f); }
    public static int altura(Context c) { return sp(c).getInt("altura", 0); }
    public static int actividad(Context c) { return sp(c).getInt("actividad", 0); }
    public static int objetivo(Context c) { return sp(c).getInt("objetivo", 0); }

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
