package gt.edu.umg.gpscamara;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gt.edu.umg.gpscamara.FotosGuardadas.BitmapUtils;
import gt.edu.umg.gpscamara.FotosGuardadas.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    private Button bttnCamara1;
    private Button buttnFotosGuardadas;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int LOCATION_PERMISSION_CODE = 101;
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLatitude;
    private double currentLongitude;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maincamara);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        bttnCamara1 = findViewById(R.id.bttnCamara1);
        buttnFotosGuardadas = findViewById(R.id.buttnFotosTomadas);

        bttnCamara1.setOnClickListener(v -> {
            verificarPermisos();
        });

        buttnFotosGuardadas.setOnClickListener(v -> {
            abrirFotosGuardadas();
        });
    }

    private void abrirFotosGuardadas() {
        Intent intent = new Intent(MainActivity.this, VerFotosActivity.class);
        startActivity(intent);
    }

    private void verificarPermisos() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, CAMERA_PERMISSION_CODE);
        } else {
            obtenerUbicacionYAbrirCamara();
        }
    }

    @SuppressLint("MissingPermission")
    private void obtenerUbicacionYAbrirCamara() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();

                        Toast.makeText(MainActivity.this,
                                "Lat: " + currentLatitude + ", Long: " + currentLongitude,
                                Toast.LENGTH_SHORT).show();

                        abrirCamara();
                    } else {
                        Toast.makeText(MainActivity.this,
                                "No se pudo obtener la ubicación",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            // Iniciar FotosTomadas con la imagen y coordenadas
            Intent intent = new Intent(MainActivity.this, FotosTomadas.class);
            intent.putExtra("imageBitmap", imageBitmap);
            intent.putExtra("currentLatitude", currentLatitude);
            intent.putExtra("currentLongitude", currentLongitude);
            startActivity(intent);
        }
    }

    private void guardarFoto(Bitmap imageBitmap) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                byte[] imageBytes = BitmapUtils.bitmapToByteArray(imageBitmap);

                // Usar DatabaseHelper en lugar de Room
                DatabaseHelper dbHelper = DatabaseHelper.getInstance(getApplicationContext());
                long id = dbHelper.insertFoto(imageBytes, currentLatitude, currentLongitude);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Foto guardada exitosamente", Toast.LENGTH_SHORT).show();
                    ImageView imageView = findViewById(R.id.imageViewFotoTomada);
                    if (imageView != null) {
                        imageView.setImageDrawable(null);
                    }
                    Button btnGuardar = findViewById(R.id.bttnGuardar);
                    Button btnEliminar = findViewById(R.id.bttnEliminar);
                    if (btnGuardar != null) btnGuardar.setEnabled(false);
                    if (btnEliminar != null) btnEliminar.setEnabled(false);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error al guardar la foto: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
                e.printStackTrace();
            }
            executorService.shutdown();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionYAbrirCamara();
            }
        }
    }
}