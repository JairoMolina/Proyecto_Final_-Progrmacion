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
import android.widget.Button;
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

import gt.edu.umg.gpscamara.FotosGuardadas.BitmapUtils;
import gt.edu.umg.gpscamara.FotosGuardadas.DatabaseHelper;
import gt.edu.umg.gpscamara.Notification.NotificationReceiver;
import gt.edu.umg.gpscamara.R;

public class FotosTomadas extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ExecutorService executorService;
    private ImageView imageViewFotoTomada;
    private Button bttnGuardar;
    private Button bttnEliminar;
    private Button bttnTomarFoto;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fotostomadas);

        // Inicializar vistas
        initializeViews();

        // Inicializar componentes
        dbHelper = DatabaseHelper.getInstance(getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();

        // Verificar permisos de notificación
        checkNotificationPermissions();

        // Obtener datos del intent
        getIntentData();

        // Configurar botones
        setupButtons();
    }

    private void initializeViews() {
        imageViewFotoTomada = findViewById(R.id.imageViewFotoTomada);
        bttnGuardar = findViewById(R.id.bttnGuardar);
        bttnEliminar = findViewById(R.id.bttnEliminar);
        bttnTomarFoto = findViewById(R.id.bttnTomarFoto);
        btnSetReminder = findViewById(R.id.btnSetReminder);
        tvReminder = findViewById(R.id.tvReminder);
    }

    private void checkNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void getIntentData() {
        currentImageBitmap = getIntent().getParcelableExtra("imageBitmap");
        currentLatitude = getIntent().getDoubleExtra("currentLatitude", 0.0);
        currentLongitude = getIntent().getDoubleExtra("currentLongitude", 0.0);

        if (currentImageBitmap != null) {
            imageViewFotoTomada.setImageBitmap(currentImageBitmap);
            bttnGuardar.setEnabled(true);
        }
    }

    private void setupButtons() {
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

        bttnGuardar.setOnClickListener(v -> {
            if (currentImageBitmap != null) {
                savePhotoToDatabase();
            }
        });

        bttnEliminar.setOnClickListener(v -> {
            clearPhoto();
        });

        bttnTomarFoto.setOnClickListener(v -> {
            openCamera();
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

    private void savePhotoToDatabase() {
        executorService.execute(() -> {
            try {
                byte[] imageBytes = BitmapUtils.bitmapToByteArray(currentImageBitmap);
                long id = dbHelper.insertFoto(imageBytes, currentLatitude, currentLongitude,
                        selectedReminderTime);

                runOnUiThread(() -> {
                    if (id != -1) {
                        if (selectedReminderTime > 0) {
                            scheduleNotification(id, selectedReminderTime);
                        }
                        Toast.makeText(this, "Foto guardada exitosamente",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error al guardar la foto",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error al guardar la foto: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
                e.printStackTrace();
            }
        });
    }

    private void scheduleNotification(long photoId, long reminderTime) {
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
                    Log.d("FotosTomadas", "Recordatorio programado para: " +
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

    private void clearPhoto() {
        imageViewFotoTomada.setImageDrawable(null);
        currentImageBitmap = null;
        bttnGuardar.setEnabled(false);
        selectedReminderTime = 0;
        tvReminder.setText("Recordatorio");
        Toast.makeText(this, "Foto eliminada", Toast.LENGTH_SHORT).show();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            currentImageBitmap = (Bitmap) extras.get("data");
            if (currentImageBitmap != null) {
                imageViewFotoTomada.setImageBitmap(currentImageBitmap);
                bttnGuardar.setEnabled(true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}