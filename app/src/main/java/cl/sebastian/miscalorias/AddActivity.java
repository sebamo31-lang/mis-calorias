package cl.sebastian.miscalorias;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Ventanita que se abre desde el botón "+ Agregar" del widget. */
public class AddActivity extends Activity {

    private EditText etName, etKcal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add);

        etName = findViewById(R.id.a_name);
        etKcal = findViewById(R.id.a_kcal);

        int goal = Prefs.goal(this);
        int total = Db.get(this).total(Prefs.today(this));
        ((TextView) findViewById(R.id.a_status)).setText(
                "Hoy llevas " + Prefs.fmt(total) + " de " + Prefs.fmt(goal) + " kcal");

        findViewById(R.id.a_btn).setOnClickListener(v -> addFromForm());
        etKcal.setOnEditorActionListener((v, actionId, e) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) { addFromForm(); return true; }
            return false;
        });

        LinearLayout box = findViewById(R.id.a_freq);
        int n = Ui.fillFrequent(this, box, this::addAndClose);
        if (n == 0) {
            findViewById(R.id.a_freq_title).setVisibility(View.GONE);
        }
        etName.requestFocus();
    }

    private void addFromForm() {
        Db.Food f = Ui.readForm(etName, etKcal);
        if (f != null) addAndClose(f);
    }

    private void addAndClose(Db.Food f) {
        String today = Prefs.today(this);
        Db.get(this).add(today, f.name, f.kcal);
        int total = Db.get(this).total(today);
        int left = Prefs.goal(this) - total;
        String msg = left >= 0
                ? "Agregado. Te quedan " + Prefs.fmt(left) + " kcal"
                : "Agregado. Vas " + Prefs.fmt(-left) + " kcal sobre la meta";
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        CaloriasWidget.updateAll(this);
        finish();
    }
}
