package cl.sebastian.miscalorias;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.text.InputType;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/** Piezas de interfaz compartidas entre las pantallas. */
final class Ui {

    private Ui() { }

    // ---- Tema ----

    static boolean isDark(Context c) {
        int t = Prefs.theme(c);
        if (t == 1) return true;
        if (t == 2) return false;
        int night = c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return night == Configuration.UI_MODE_NIGHT_YES;
    }

    /** Se llama antes de super.onCreate() en cada pantalla. */
    static void applyTheme(Activity a, boolean dialog) {
        boolean dark = isDark(a);
        if (dialog) a.setTheme(dark ? R.style.Dialogo_Oscuro : R.style.Dialogo_Claro);
        else a.setTheme(dark ? R.style.AppTheme_Oscuro : R.style.AppTheme_Claro);
    }

    static int attr(Context c, int attrId) {
        TypedValue tv = new TypedValue();
        c.getTheme().resolveAttribute(attrId, tv, true);
        return tv.data;
    }

    static int dp(Context c, int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                c.getResources().getDisplayMetrics());
    }

    // ---- Botones rápidos ----

    /**
     * Llena la fila de botones: primero "Mis alimentos"; si aún no guardas ninguno,
     * muestra tus comidas más frecuentes. Devuelve 0 si no hay nada, 1 si son
     * alimentos guardados y 2 si son frecuentes.
     */
    static int fillChips(Activity a, LinearLayout box, Consumer<Db.Food> onTap) {
        box.removeAllViews();
        List<Db.Food> foods = Db.get(a).foods(null);
        int mode = 1;
        if (foods.isEmpty()) {
            foods = Db.get(a).frequent(12);
            mode = 2;
        }
        if (foods.isEmpty()) return 0;
        int bg = attr(a, R.attr.cChip);
        int fg = attr(a, R.attr.cChipTexto);
        for (Db.Food f : foods) {
            Button b = new Button(a);
            b.setAllCaps(false);
            b.setText(f.name + "\n" + Prefs.fmt(f.kcal) + " kcal");
            b.setTextSize(13);
            b.setBackgroundTintList(ColorStateList.valueOf(bg));
            b.setTextColor(fg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.rightMargin = dp(a, 6);
            b.setLayoutParams(lp);
            b.setOnClickListener(v -> onTap.accept(f));
            box.addView(b);
        }
        return mode;
    }

    /** Sugiere tus alimentos guardados al escribir y completa las calorías. */
    static void setupAutocomplete(Activity a, AutoCompleteTextView name, EditText kcal) {
        List<String> names = new ArrayList<>();
        for (Db.Food f : Db.get(a).foods(null)) names.add(f.name);
        name.setAdapter(new ArrayAdapter<>(a, android.R.layout.simple_dropdown_item_1line, names));
        name.setOnItemClickListener((parent, view, pos, id) -> {
            Db.Food f = Db.get(a).findFood(String.valueOf(parent.getItemAtPosition(pos)));
            if (f != null) {
                kcal.setText(String.valueOf(f.kcal));
                kcal.setSelection(kcal.getText().length());
                kcal.requestFocus();
            }
        });
    }

    // ---- Diálogos ----

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

    /** Diálogo para crear o editar un alimento guardado. existing = null para uno nuevo. */
    static void foodDialog(Activity a, Db.Food existing, Runnable onSaved) {
        EditText name = new EditText(a);
        name.setHint("Nombre (ej: Handroll de pollo)");
        name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        EditText kcal = new EditText(a);
        kcal.setHint("Calorías (kcal)");
        kcal.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (existing != null) {
            name.setText(existing.name);
            kcal.setText(String.valueOf(existing.kcal));
        }
        LinearLayout wrap = new LinearLayout(a);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(a, 24), dp(a, 8), dp(a, 24), 0);
        wrap.addView(name);
        wrap.addView(kcal);
        AlertDialog d = new AlertDialog.Builder(a)
                .setTitle(existing == null ? "Nuevo alimento" : "Editar alimento")
                .setView(wrap)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();
        d.setOnShowListener(x -> d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Db.Food f = readForm(name, kcal);
            if (f == null) return;
            if (existing == null) Db.get(a).saveFood(f.name, f.kcal);
            else Db.get(a).updateFood(existing.id, f.name, f.kcal);
            d.dismiss();
            onSaved.run();
        }));
        d.show();
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
