package gt.edu.umg.gpscamara.Notification;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import gt.edu.umg.gpscamara.R;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;


public class NotificationSettingsActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        checkNotificationPermissions();

        findViewById(R.id.btnOpenSettings).setOnClickListener(v -> openNotificationSettings());
    }

    private void checkNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this,
                    "android.permission.POST_NOTIFICATIONS") !=
                    PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        "android.permission.POST_NOTIFICATIONS")) {
                    showPermissionExplanationDialog();
                } else {
                    requestNotificationPermission();
                }
            }
        }
    }

    private void showPermissionExplanationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permiso de Notificaciones")
                .setMessage("Necesitamos tu permiso para enviarte recordatorios sobre tus fotos guardadas.")
                .setPositiveButton("Permitir", (dialog, which) -> {
                    requestNotificationPermission();
                })
                .setNegativeButton("No permitir", (dialog, which) -> {
                    Toast.makeText(this,
                            "Las notificaciones son necesarias para los recordatorios",
                            Toast.LENGTH_LONG).show();
                })
                .create()
                .show();
    }

    private void requestNotificationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{"android.permission.POST_NOTIFICATIONS"},
                PERMISSION_REQUEST_CODE);
    }

    private void openNotificationSettings() {
        try {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            } else {
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
            }
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this,
                    "No se pudo abrir la configuración de notificaciones",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notificaciones activadas",
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                showSettingsPrompt();
            }
        }
    }

    private void showSettingsPrompt() {
        new AlertDialog.Builder(this)
                .setTitle("Permiso Necesario")
                .setMessage("Las notificaciones son necesarias para los recordatorios. " +
                        "¿Deseas habilitarlas en la configuración?")
                .setPositiveButton("Ir a Configuración", (dialog, which) -> {
                    openNotificationSettings();
                })
                .setNegativeButton("Cancelar", null)
                .create()
                .show();
    }
}