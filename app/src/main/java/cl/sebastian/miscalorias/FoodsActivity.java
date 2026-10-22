package cl.sebastian.miscalorias;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/** Lista de alimentos guardados: agregar a hoy, crear, editar y borrar. */
public class FoodsActivity extends Activity {

    private EditText etBuscar;
    private LinearLayout lista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Ui.applyTheme(this, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.foods);
        setTitle("Mis alimentos");

        etBuscar = findViewById(R.id.f_buscar);
        lista = findViewById(R.id.f_lista);

        findViewById(R.id.f_nuevo).setOnClickListener(v -> Ui.foodDialog(this, null, this::refresh));
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { refresh(); }
            @Override public void afterTextChanged(Editable s) { }
        });
        refresh();
    }

    private void refresh() {
        lista.removeAllViews();
        List<Db.Food> foods = Db.get(this).foods(etBuscar.getText().toString());
        if (foods.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(etBuscar.getText().length() > 0
                    ? "No encontré ese alimento."
                    : "Aún no guardas alimentos.\nToca \"+ Nuevo\" o marca \"Guardar en Mis alimentos\" al agregar una comida.");
            empty.setTextColor(Ui.attr(this, R.attr.cTextoSec));
            lista.addView(empty);
            return;
        }
        LayoutInflater inf = getLayoutInflater();
        for (Db.Food f : foods) {
            View row = inf.inflate(R.layout.row, lista, false);
            ((TextView) row.findViewById(R.id.r_name)).setText(f.name);
            ((TextView) row.findViewById(R.id.r_sub)).setText("Toca para agregar a hoy");
            ((TextView) row.findViewById(R.id.r_kcal)).setText(Prefs.fmt(f.kcal) + " kcal");
            row.setOnClickListener(v -> {
                Db.get(this).add(Prefs.today(this), f.name, f.kcal);
                CaloriasWidget.updateAll(this);
                int left = Prefs.goal(this) - Db.get(this).total(Prefs.today(this));
                Toast.makeText(this, "Agregado a hoy. " + (left >= 0
                        ? "Te quedan " + Prefs.fmt(left) + " kcal"
                        : "Vas " + Prefs.fmt(-left) + " kcal sobre la meta"), Toast.LENGTH_SHORT).show();
            });
            row.setOnLongClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle(f.name)
                        .setItems(new String[]{"Editar", "Borrar"}, (d, w) -> {
                            if (w == 0) {
                                Ui.foodDialog(this, f, this::refresh);
                            } else {
                                new AlertDialog.Builder(this)
                                        .setTitle("¿Borrar " + f.name + "?")
                                        .setMessage("Solo se quita de Mis alimentos; lo que ya registraste se mantiene.")
                                        .setNegativeButton("Cancelar", null)
                                        .setPositiveButton("Borrar", (d2, w2) -> {
                                            Db.get(this).deleteFood(f.id);
                                            refresh();
                                        })
                                        .show();
                            }
                        })
                        .show();
                return true;
            });
            lista.addView(row);
        }
    }
}
