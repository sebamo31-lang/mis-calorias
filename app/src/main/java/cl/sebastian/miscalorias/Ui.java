package cl.sebastian.miscalorias;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.InputType;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/** Piezas de interfaz compartidas entre la pantalla principal y la ventana rápida. */
final class Ui {

    private Ui() { }

    static int dp(Context c, int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                c.getResources().getDisplayMetrics());
    }

    /** Llena la fila de botones con tus comidas frecuentes. Devuelve cuántas puso. */
    static int fillFrequent(Activity a, LinearLayout box, Consumer<Db.Food> onTap) {
        box.removeAllViews();
        List<Db.Food> foods = Db.get(a).frequent(12);
        for (Db.Food f : foods) {
            Button b = new Button(a);
            b.setAllCaps(false);
            b.setText(f.name + "\n" + Prefs.fmt(f.kcal) + " kcal");
            b.setTextSize(13);
            b.setBackgroundTintList(ColorStateList.valueOf(0xFFFFFFFF));
            b.setTextColor(0xFF1F2933);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.rightMargin = dp(a, 6);
            b.setLayoutParams(lp);
            b.setOnClickListener(v -> onTap.accept(f));
            box.addView(b);
        }
        return foods.size();
    }

    static void askNumber(Activity a, String title, String msg, int current,
                          int min, int max, IntConsumer onOk) {
        EditText et = new EditText(a);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        et.setText(String.valueOf(current));
        et.setSelectAllOnFocus(true);
        LinearLayout wrap = new LinearLayout(a);
        wrap.setPadding(dp(a, 24), dp(a, 8), dp(a, 24), 0);
        wrap.addView(et, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        new AlertDialog.Builder(a)
                .setTitle(title)
                .setMessage(msg)
                .setView(wrap)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", (d, w) -> {
                    try {
                        int v = Integer.parseInt(et.getText().toString().trim());
                        if (v >= min && v <= max) onOk.accept(v);
                    } catch (NumberFormatException ignored) { }
                })
                .show();
    }

    /** Lee nombre y calorías de dos casillas. Devuelve null y marca el error si falta algo. */
    static Db.Food readForm(EditText name, EditText kcal) {
        String n = name.getText().toString().trim();
        String k = kcal.getText().toString().trim();
        if (n.isEmpty()) {
            name.setError("Escribe qué comiste");
            name.requestFocus();
            return null;
        }
        int v;
        try {
            v = Integer.parseInt(k);
        } catch (NumberFormatException e) {
            kcal.setError("Escribe las calorías");
            kcal.requestFocus();
            return null;
        }
        if (v <= 0 || v > 10000) {
            kcal.setError("Revisa el número");
            kcal.requestFocus();
            return null;
        }
        Db.Food f = new Db.Food();
        f.name = n;
        f.kcal = v;
        return f;
    }
}
