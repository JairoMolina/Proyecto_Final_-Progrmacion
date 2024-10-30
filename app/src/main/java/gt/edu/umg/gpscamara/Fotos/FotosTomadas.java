package gt.edu.umg.gpscamara.Fotos;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.util.Log;

import gt.edu.umg.gpscamara.BaseDatos.BitmapUtils;
import gt.edu.umg.gpscamara.BaseDatos.DatabaseHelper;
import gt.edu.umg.gpscamara.Notification.NotificationReceiver;
import gt.edu.umg.gpscamara.R;

public class FotosTomadas extends AppCompatActivity {
    private static final String TAG = "FotosTomadas";
    private DatabaseHelper dbHelper;
    private ExecutorService executorService;
    private ImageView imageViewFotoTomada;
    private Button bttnGuardar;
    private Button bttnEliminar;
    private Button bttnTomarFoto;
    private EditText editTextNombre;
    private CheckBox checkBoxAcepto;
    private Button btnSetReminder;
    private TextView tvReminder;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    private double currentLatitude;
    private double currentLongitude;
    private Bitmap currentImageBitmap;
    private long selectedReminderTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "Iniciando onCreate");
            setContentView(R.layout.activity_fotostomadas);

            initializeViews();
            initializeDatabase();
            getIntentData();
            setupListeners();
            checkNotificationPermissions();

            Log.d(TAG, "onCreate completado exitosamente");
        } catch (Exception e) {
            Log.e(TAG, "Error en onCreate: " + e.getMessage());
            Toast.makeText(this, "Error al iniciar la actividad", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            imageViewFotoTomada = findViewById(R.id.imageViewFotoTomada);
            bttnGuardar = findViewById(R.id.bttnGuardar);
            bttnEliminar = findViewById(R.id.bttnEliminar);
            bttnTomarFoto = findViewById(R.id.bttnTomarFoto);
            editTextNombre = findViewById(R.id.editTextNombre);
            checkBoxAcepto = findViewById(R.id.checkBoxAcepto);
            btnSetReminder = findViewById(R.id.btnSetReminder);
            tvReminder = findViewById(R.id.tvReminder);

            Log.d(TAG, "Vistas inicializadas correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando vistas: " + e.getMessage());
            throw e;
        }
    }

    private void checkNotificationPermissions() {  // Nuevo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void initializeDatabase() {
        dbHelper = DatabaseHelper.getInstance(getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();
    }

    private void getIntentData() {
        try {
            Intent intent = getIntent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                currentImageBitmap = intent.getParcelableExtra("imageBitmap", Bitmap.class);
            } else {
                currentImageBitmap = intent.getParcelableExtra("imageBitmap");
            }
            currentLatitude = intent.getDoubleExtra("currentLatitude", 0.0);
            currentLongitude = intent.getDoubleExtra("currentLongitude", 0.0);

            Log.d(TAG, String.format("Datos recibidos - Lat: %f, Long: %f",
                    currentLatitude, currentLongitude));

            if (currentImageBitmap != null) {
                imageViewFotoTomada.setImageBitmap(currentImageBitmap);
                Log.d(TAG, "Imagen cargada correctamente");
            } else {
                Log.w(TAG, "No se recibió imagen en el intent");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo datos del intent: " + e.getMessage());
            Toast.makeText(this, "Error al cargar los datos", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        checkBoxAcepto.setOnCheckedChangeListener((buttonView, isChecked) -> {
            validateInputs();
            Log.d(TAG, "Estado del checkbox cambiado: " + isChecked);
        });

        editTextNombre.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
                Log.d(TAG, "Nombre actualizado: " + s.toString());
            }
        });

        bttnGuardar.setOnClickListener(v -> {
            if (currentImageBitmap != null) {
                String nombre = editTextNombre.getText().toString();
                boolean aceptado = checkBoxAcepto.isChecked();
                Log.d(TAG, "Intentando guardar foto con nombre: " + nombre);
                savePhotoToDatabase(currentImageBitmap, nombre, currentLatitude,
                        currentLongitude, aceptado);
            }
        });

        bttnEliminar.setOnClickListener(v -> clearPhoto());
        bttnTomarFoto.setOnClickListener(v -> openCamera());
        btnSetReminder.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Se requieren permisos de notificación",
                        Toast.LENGTH_LONG).show();
                return;
            }
            showDateTimePicker();
        });
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    new TimePickerDialog(this,
                            (view1, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                selectedReminderTime = calendar.getTimeInMillis();

                                SimpleDateFormat sdf = new SimpleDateFormat(
                                        "dd/MM/yyyy HH:mm",
                                        Locale.getDefault()
                                );
                                tvReminder.setText("Recordatorio: " + sdf.format(calendar.getTime()));

                                Toast.makeText(this,
                                        "Recordatorio establecido para: " + sdf.format(calendar.getTime()),
                                        Toast.LENGTH_LONG).show();
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    ).show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void validateInputs() {
        String nombre = editTextNombre.getText().toString();
        boolean aceptado = checkBoxAcepto.isChecked();
        boolean isValid = !nombre.isEmpty() && currentImageBitmap != null && aceptado;
        bttnGuardar.setEnabled(isValid);
        Log.d(TAG, "Validación de inputs - Válido: " + isValid);
    }

    private void clearPhoto() {
        imageViewFotoTomada.setImageDrawable(null);
        currentImageBitmap = null;
        bttnGuardar.setEnabled(false);
        editTextNombre.setText("");
        checkBoxAcepto.setChecked(false);
        selectedReminderTime = 0;
        tvReminder.setText("Recordatorio");
        Log.d(TAG, "Foto eliminada");
        Toast.makeText(this, "Foto eliminada", Toast.LENGTH_SHORT).show();
    }

    private void openCamera() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                Log.d(TAG, "Cámara iniciada");
            } else {
                Log.e(TAG, "No se encontró app de cámara");
                Toast.makeText(this, "No se puede acceder a la cámara",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al abrir la cámara: " + e.getMessage());
            Toast.makeText(this, "Error al abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleNotification(long photoId, long reminderTime) {  // Nuevo
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("photoId", photoId);
        intent.putExtra("scheduledTime", reminderTime);
        intent.putExtra("currentTime", System.currentTimeMillis());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) photoId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            reminderTime,
                            pendingIntent
                    );
                    Log.d(TAG, "Recordatorio programado para: " +
                            new Date(reminderTime).toString());
                } else {
                    Intent intent2 = new Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:" + getPackageName())
                    );
                    startActivity(intent2);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                );
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    currentImageBitmap = (Bitmap) extras.get("data");
                    if (currentImageBitmap != null) {
                        imageViewFotoTomada.setImageBitmap(currentImageBitmap);
                        validateInputs();
                        Log.d(TAG, "Foto capturada exitosamente");
                    } else {
                        Log.e(TAG, "Error: bitmap es null");
                        Toast.makeText(this, "Error al capturar la imagen",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en onActivityResult: " + e.getMessage());
            Toast.makeText(this, "Error al procesar la foto", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePhotoToDatabase(Bitmap imageBitmap, String nombre, double latitude,
                                     double longitude, boolean aceptado) {
        executorService.execute(() -> {
            try {
                byte[] imageBytes = BitmapUtils.bitmapToByteArray(imageBitmap);
                long id = dbHelper.insertFoto(imageBytes, nombre, latitude, longitude,
                        selectedReminderTime, aceptado);

                runOnUiThread(() -> {
                    if (id != -1) {
                        Log.d(TAG, "Foto guardada con ID: " + id);
                        if (selectedReminderTime > 0) {
                            scheduleNotification(id, selectedReminderTime);
                        }
                        Toast.makeText(this, "Foto guardada exitosamente",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Log.e(TAG, "Error al guardar en base de datos");
                        Toast.makeText(this, "Error al guardar la foto",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error guardando foto: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error al guardar la foto: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            Log.d(TAG, "ExecutorService apagado");
        }
    }
}