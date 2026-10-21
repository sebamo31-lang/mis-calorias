package cl.sebastian.miscalorias;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/** Widget de pantalla de inicio: cuánto llevas hoy y cuánto te queda. */
public class CaloriasWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        updateAll(context);
    }

    public static void updateAll(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        int[] ids = m.getAppWidgetIds(new ComponentName(c, CaloriasWidget.class));
        if (ids == null || ids.length == 0) return;

        int goal = Prefs.goal(c);
        int total = Db.get(c).total(Prefs.today(c));
        int left = goal - total;

        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget);
        v.setTextViewText(R.id.w_total, Prefs.fmt(total));
        v.setTextViewText(R.id.w_goal, "de " + Prefs.fmt(goal) + " kcal");
        v.setProgressBar(R.id.w_progress, goal, Math.min(total, goal), false);
        if (left >= 0) {
            v.setTextViewText(R.id.w_left, "Te quedan " + Prefs.fmt(left) + " kcal");
            v.setTextColor(R.id.w_left, 0xFFB8F5C8);
        } else {
            v.setTextViewText(R.id.w_left, "Te pasaste por " + Prefs.fmt(-left) + " kcal");
            v.setTextColor(R.id.w_left, 0xFFFFA48F);
        }

        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        v.setOnClickPendingIntent(R.id.w_root,
                PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class), flags));
        Intent add = new Intent(c, AddActivity.class);
        add.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        v.setOnClickPendingIntent(R.id.w_add, PendingIntent.getActivity(c, 1, add, flags));

        m.updateAppWidget(ids, v);
    }
}
