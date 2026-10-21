package cl.sebastian.miscalorias;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {

    private static final int MENU_META = 1;
    private static final int MENU_CORTE = 2;

    /** 0 = hoy, -1 = ayer, etc. */
    private int offset = 0;

    private TextView tvDate, tvTotal, tvGoal, tvLeft, tvFreqTitle;
    private ProgressBar pb;
    private EditText etName, etKcal;
    private Button btnNext;
    private HorizontalScrollView svFreq;
    private LinearLayout boxFreq, boxList, boxHist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tvDate = findViewById(R.id.tv_date);
        tvTotal = findViewById(R.id.tv_total);
        tvGoal = findViewById(R.id.tv_goal);
        tvLeft = findViewById(R.id.tv_left);
        tvFreqTitle = findViewById(R.id.tv_freq_title);
        pb = findViewById(R.id.pb);
        etName = findViewById(R.id.et_name);
        etKcal = findViewById(R.id.et_kcal);
        btnNext = findViewById(R.id.btn_next);
        svFreq = findViewById(R.id.sv_freq);
        boxFreq = findViewById(R.id.box_freq);
        boxList = findViewById(R.id.box_list);
        boxHist = findViewById(R.id.box_hist);

        findViewById(R.id.btn_prev).setOnClickListener(v -> { offset--; refresh(); });
        btnNext.setOnClickListener(v -> {
            if (offset < 0) { offset++; refresh(); }
        });
        findViewById(R.id.btn_add).setOnClickListener(v -> addFromForm());
        etKcal.setOnEditorActionListener((v, actionId, e) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) { addFromForm(); return true; }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private String day() {
        return Prefs.dayKey(Prefs.dayCal(this, offset));
    }

    private void addFromForm() {
        Db.Food f = Ui.readForm(etName, etKcal);
        if (f == null) return;
        add(f);
        etName.setText("");
        etKcal.setText("");
        etName.requestFocus();
    }

    private void add(Db.Food f) {
        Db.get(this).add(day(), f.name, f.kcal);
        Toast.makeText(this, "Agregado: " + f.name + " (" + Prefs.fmt(f.kcal) + " kcal)",
                Toast.LENGTH_SHORT).show();
        refresh();
    }

    private void refresh() {
        String day = day();
        String fecha = Prefs.pretty(day, "EEEE d 'de' MMMM");
        String label = offset == 0 ? "Hoy" : offset == -1 ? "Ayer" : Prefs.cap(fecha);
        tvDate.setText(offset >= -1 ? label + "\n" + fecha : label);
        btnNext.setEnabled(offset < 0);
        btnNext.setAlpha(offset < 0 ? 1f : 0.25f);

        int goal = Prefs.goal(this);
        int total = Db.get(this).total(day);
        int left = goal - total;
        tvTotal.setText(Prefs.fmt(total));
        tvGoal.setText("de " + Prefs.fmt(goal) + " kcal");
        pb.setMax(goal);
        pb.setProgress(Math.min(total, goal));
        if (left >= 0) {
            tvLeft.setText("Te quedan " + Prefs.fmt(left) + " kcal");
            tvLeft.setTextColor(0xFFB8F5C8);
        } else {
            tvLeft.setText("Te pasaste por " + Prefs.fmt(-left) + " kcal");
            tvLeft.setTextColor(0xFFFFA48F);
        }

        int n = Ui.fillFrequent(this, boxFreq, this::add);
        tvFreqTitle.setVisibility(n > 0 ? View.VISIBLE : View.GONE);
        svFreq.setVisibility(n > 0 ? View.VISIBLE : View.GONE);

        fillList(day);
        fillHistory(goal);
        CaloriasWidget.updateAll(this);
    }

    private void fillList(String day) {
        boxList.removeAllViews();
        List<Db.Entry> entries = Db.get(this).list(day);
        if (entries.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Aún no registras nada este día.");
            empty.setTextColor(0xFF6B7680);
            empty.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
            boxList.addView(empty);
            return;
        }
        LayoutInflater inf = getLayoutInflater();
        SimpleDateFormat hora = new SimpleDateFormat("HH:mm", Prefs.CL);
        for (Db.Entry e : entries) {
            View row = inf.inflate(R.layout.row, boxList, false);
            ((TextView) row.findViewById(R.id.r_name)).setText(e.name);
            ((TextView) row.findViewById(R.id.r_sub)).setText("Registrado a las " + hora.format(new Date(e.ts)));
            ((TextView) row.findViewById(R.id.r_kcal)).setText(Prefs.fmt(e.kcal) + " kcal");
            row.setOnLongClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("¿Borrar esta comida?")
                        .setMessage(e.name + " · " + Prefs.fmt(e.kcal) + " kcal")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Borrar", (d, w) -> {
                            Db.get(this).delete(e.id);
                            refresh();
                        })
                        .show();
                return true;
            });
            boxList.addView(row);
        }
    }

    private void fillHistory(int goal) {
        boxHist.removeAllViews();
        List<Db.DayTotal> days = Db.get(this).history(7);
        if (days.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Aquí vas a ver el total de cada día.");
            empty.setTextColor(0xFF6B7680);
            boxHist.addView(empty);
            return;
        }
        LayoutInflater inf = getLayoutInflater();
        for (Db.DayTotal d : days) {
            View row = inf.inflate(R.layout.row, boxHist, false);
            boolean ok = d.kcal <= goal;
            ((TextView) row.findViewById(R.id.r_name)).setText(Prefs.cap(Prefs.pretty(d.day, "EEEE d MMM")));
            ((TextView) row.findViewById(R.id.r_sub)).setText(ok
                    ? "Dentro de la meta"
                    : "Sobre la meta por " + Prefs.fmt(d.kcal - goal) + " kcal");
            TextView k = row.findViewById(R.id.r_kcal);
            k.setText(Prefs.fmt(d.kcal) + " kcal");
            k.setTextColor(ok ? 0xFF2E7D4F : 0xFFC0472A);
            boxHist.addView(row);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_META, 0, "Meta diaria");
        menu.add(0, MENU_CORTE, 1, "Hora de corte del día");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_META) {
            Ui.askNumber(this, "Meta diaria",
                    "¿Cuántas calorías quieres comer como máximo al día?",
                    Prefs.goal(this), 800, 6000, v -> { Prefs.setGoal(this, v); refresh(); });
            return true;
        }
        if (item.getItemId() == MENU_CORTE) {
            Ui.askNumber(this, "Hora de corte del día",
                    "Si te acuestas tarde, lo que comas antes de esta hora de la madrugada " +
                    "cuenta para el día anterior.\nEjemplo: 4 = hasta las 4:00 AM (0 a 8).",
                    Prefs.cutoff(this), 0, 8, v -> { Prefs.setCutoff(this, v); offset = 0; refresh(); });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
