package cl.sebastian.miscalorias;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/** Calcula IMC, gasto diario y una meta de calorías con déficit. */
public class CalcActivity extends Activity {

    private static final String[] ACTIVIDADES = {
            "Poco o nada (casi todo el día sentado)",
            "Ligera (caminas o haces ejercicio 1–3 días)",
            "Moderada (ejercicio 3–5 días o trabajo de pie)",
            "Alta (ejercicio fuerte 6–7 días o trabajo pesado)"
    };
    private static final double[] FACTORES = {1.2, 1.375, 1.55, 1.725};
    private static final int[] DEFICITS = {500, 300, 0};

    private RadioGroup rgSexo, rgObjetivo;
    private EditText etEdad, etPeso, etAltura;
    private Spinner spActividad;
    private View boxResultado;
    private TextView tvMeta, tvDetalle;
    private Button btnUsar;
    private int metaCalculada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Ui.applyTheme(this, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calc);
        setTitle("Calcular mis calorías");

        rgSexo = findViewById(R.id.c_sexo);
        rgObjetivo = findViewById(R.id.c_objetivo);
        etEdad = findViewById(R.id.c_edad);
        etPeso = findViewById(R.id.c_peso);
        etAltura = findViewById(R.id.c_altura);
        spActividad = findViewById(R.id.c_actividad);
        boxResultado = findViewById(R.id.c_resultado);
        tvMeta = findViewById(R.id.c_meta);
        tvDetalle = findViewById(R.id.c_detalle);
        btnUsar = findViewById(R.id.c_usar);

        spActividad.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, ACTIVIDADES));

        // Si ya los habías ingresado, se cargan de nuevo.
        if (Prefs.hasProfile(this)) {
            ((RadioButton) findViewById(Prefs.mujer(this) ? R.id.c_mujer : R.id.c_hombre)).setChecked(true);
            etEdad.setText(String.valueOf(Prefs.edad(this)));
            float p = Prefs.peso(this);
            etPeso.setText(p == Math.floor(p) ? String.valueOf((int) p) : String.valueOf(p));
            etAltura.setText(String.valueOf(Prefs.altura(this)));
            spActividad.setSelection(Math.min(Prefs.actividad(this), ACTIVIDADES.length - 1));
            int obj = Prefs.objetivo(this);
            int[] ids = {R.id.c_def500, R.id.c_def300, R.id.c_mantener};
            ((RadioButton) findViewById(ids[Math.min(obj, 2)])).setChecked(true);
        }

        findViewById(R.id.c_calcular).setOnClickListener(v -> calcular());
        btnUsar.setOnClickListener(v -> {
            Prefs.setGoal(this, metaCalculada);
            CaloriasWidget.updateAll(this);
            Toast.makeText(this, "Listo: tu meta ahora es " + Prefs.fmt(metaCalculada) + " kcal",
                    Toast.LENGTH_LONG).show();
            finish();
        });
    }

    private void calcular() {
        int edad = leerEntero(etEdad, 14, 100, "Edad entre 14 y 100");
        Float peso = leerDecimal(etPeso, 30, 300, "Peso entre 30 y 300 kg");
        int altura = leerEntero(etAltura, 120, 230, "Altura entre 120 y 230 cm");
        if (edad < 0 || peso == null || altura < 0) return;

        boolean mujer = rgSexo.getCheckedRadioButtonId() == R.id.c_mujer;
        int act = spActividad.getSelectedItemPosition();
        int objId = rgObjetivo.getCheckedRadioButtonId();
        int obj = objId == R.id.c_def300 ? 1 : objId == R.id.c_mantener ? 2 : 0;
        Prefs.saveProfile(this, mujer, edad, peso, altura, act, obj);

        // Mifflin-St Jeor
        double tmb = 10 * peso + 6.25 * altura - 5 * edad + (mujer ? -161 : 5);
        double gasto = tmb * FACTORES[act];
        int minimo = mujer ? 1200 : 1500;
        int meta = (int) Math.round((gasto - DEFICITS[obj]) / 10.0) * 10;
        boolean ajustada = false;
        if (meta < minimo) { meta = minimo; ajustada = true; }
        metaCalculada = meta;

        double m = altura / 100.0;
        double imc = peso / (m * m);
        String cat = imc < 18.5 ? "bajo peso" : imc < 25 ? "peso normal"
                : imc < 30 ? "sobrepeso" : "obesidad";

        int deficitReal = (int) Math.round(gasto) - meta;
        StringBuilder sb = new StringBuilder();
        sb.append("• IMC: ").append(String.format(Prefs.CL, "%.1f", imc)).append(" (").append(cat).append(")\n");
        sb.append("• Tu cuerpo en reposo gasta unas ").append(Prefs.fmt((int) Math.round(tmb))).append(" kcal\n");
        sb.append("• Con tu actividad gastas unas ").append(Prefs.fmt((int) Math.round(gasto))).append(" kcal al día (mantención)\n");
        if (deficitReal > 0) {
            double kgSemana = deficitReal * 7 / 7700.0;
            sb.append("• Comiendo ").append(Prefs.fmt(meta)).append(" kcal bajarías aprox. ")
                    .append(String.format(Prefs.CL, "%.1f", kgSemana)).append(" kg por semana");
        } else {
            sb.append("• Con ").append(Prefs.fmt(meta)).append(" kcal mantienes tu peso");
        }
        if (ajustada) {
            sb.append("\n\nSubí la meta a ").append(Prefs.fmt(minimo))
                    .append(" kcal: comer menos que eso sin supervisión no es recomendable.");
        }

        tvMeta.setText(Prefs.fmt(meta) + " kcal");
        tvDetalle.setText(sb.toString());
        btnUsar.setText("Usar " + Prefs.fmt(meta) + " kcal como mi meta");
        boxResultado.setVisibility(View.VISIBLE);

        View focus = getCurrentFocus();
        if (focus != null) {
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }
        ScrollView sv = (ScrollView) boxResultado.getParent().getParent();
        sv.post(() -> sv.smoothScrollTo(0, boxResultado.getTop()));
    }

    private int leerEntero(EditText et, int min, int max, String error) {
        try {
            int v = Integer.parseInt(et.getText().toString().trim());
            if (v >= min && v <= max) return v;
        } catch (NumberFormatException ignored) { }
        et.setError(error);
        et.requestFocus();
        return -1;
    }

    private Float leerDecimal(EditText et, float min, float max, String error) {
        try {
            float v = Float.parseFloat(et.getText().toString().trim().replace(',', '.'));
            if (v >= min && v <= max) return v;
        } catch (NumberFormatException ignored) { }
        et.setError(error);
        et.requestFocus();
        return null;
    }
}
