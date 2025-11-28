package com.example.a0489_ra3_pt2_podometre;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private TextView pasosRecorreguts;
    private TextView metresRecorreguts;
    private TextView tvObjectiu;

    private Button btReiniciar;
    private Button btObjectiu;
    private ProgressBar cerclePassos;

    private ProgressBar cercleMetres;

    private int pasos = 0, objectiu = 50;
    private double metres = 0;
    private boolean pasDetectat = false;

    // Umbrals realistes para quan es detecta un pas
    private static final double DETECTOR_PASOS = 12.0; // Valor per detectar quan fa una pas la persona
    // Valor per reiniciar el contador, un cop detectat el pas
    private static final double PAS_REINICIAT = 10.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pasosRecorreguts = findViewById(R.id.pasosRecorreguts);
        metresRecorreguts = findViewById(R.id.metresRecorreguts);
        tvObjectiu = findViewById(R.id.objetciu);
        btReiniciar = findViewById(R.id.btReiniciar);
        btObjectiu = findViewById(R.id.btObjectiu);


        cerclePassos = findViewById(R.id.cerclePassos);
        cerclePassos.setProgress(0);

        cercleMetres = findViewById(R.id.cercleMetres);
        cercleMetres.setProgress(0);


        tvObjectiu.setText(objectiu + " passos");

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        btReiniciar.setOnClickListener(v -> {
            reiniciarValors();
            Toast.makeText(MainActivity.this,
                    "Comptador reiniciat",
                    Toast.LENGTH_SHORT).show();
        });

        btObjectiu.setOnClickListener(v -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
            android.view.LayoutInflater inflater = getLayoutInflater();
            android.view.View popupView = inflater.inflate(R.layout.objectius, null);
            builder.setView(popupView);

            android.app.AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();

            NumberPicker numberPicker = popupView.findViewById(R.id.numberPicker);
            Button btnGuardar = popupView.findViewById(R.id.btnGuardar);

            numberPicker.setMinValue(10);
            numberPicker.setMaxValue(5000);
            numberPicker.setValue(objectiu);
            numberPicker.setWrapSelectorWheel(false);

            btnGuardar.setOnClickListener(x -> {
                objectiu = numberPicker.getValue();
                tvObjectiu.setText(objectiu + " passos");

                Toast.makeText(MainActivity.this,
                        "Objectiu actualitzat a " + objectiu + " passos",
                        Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            });

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // ✔ Cálculo oficial pedido en el enunciado
            double magnitude = Math.sqrt(x*x + y*y + z*z);

            // Detectar subida fuerte → pico
            if (magnitude > DETECTOR_PASOS && !pasDetectat) {
                pasos++;
                pasDetectat = true;

                pasosRecorreguts.setText(String.valueOf(pasos));
                metres = (pasos * 0.8);
                metresRecorreguts.setText(String.format("%.2f", metres));

                int progressPasos = (int) (((double) pasos / objectiu) * 100);
                cerclePassos.setProgress(progressPasos);

                double progressMetres = ((metres / (objectiu * 0.8)) * 100);
                cercleMetres.setProgress((int) progressMetres);

                if (pasos == objectiu) {
                    Toast.makeText(this, "Objectiu aconseguit!", Toast.LENGTH_LONG).show();
                } else if (pasos > objectiu) {
                    reiniciarValors();
                }
            }

            // Cuando baja → permite detectar un nuevo paso
            if (magnitude < PAS_REINICIAT) {
                pasDetectat = false;
            }

            //lastMagnitude = magnitude;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void reiniciarValors() {
        pasos = 0;
        pasosRecorreguts.setText("0");
        metresRecorreguts.setText("0");
        cerclePassos.setProgress(0);
        cercleMetres.setProgress(0);
    }
}
