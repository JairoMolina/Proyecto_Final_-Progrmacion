package gt.edu.umg.gpscamara;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

;
import gt.edu.umg.gpscamara.Fotos.FotosTomadas;
import gt.edu.umg.gpscamara.Fotos.VerFotosActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button bttnCamara1;
    private Button buttnFotosGuardadas;

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int LOCATION_PERMISSION_CODE = 101;
    private static final int CALENDAR_PERMISSION_CODE = 102;
    private static final int CAMERA_REQUEST = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLatitude;
    private double currentLongitude;
    private CheckBox checkBoxAcepto;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maincamara);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        bttnCamara1 = findViewById(R.id.bttnCamara1);
        buttnFotosGuardadas = findViewById(R.id.buttnFotosTomadas);
        checkBoxAcepto = findViewById(R.id.checkBoxAcepto);

        bttnCamara1.setOnClickListener(v -> verificarPermisos());
        buttnFotosGuardadas.setOnClickListener(v -> abrirFotosGuardadas());
    }

    private void verificarPermisos() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR
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
                        Log.d(TAG, "Ubicación obtenida: " + currentLatitude + ", " + currentLongitude);
                        abrirCamara();
                    } else {
                        Log.e(TAG, "No se pudo obtener la ubicación");
                        Toast.makeText(this, "No se pudo obtener la ubicación",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener ubicación: " + e.getMessage());
                    Toast.makeText(this, "Error al obtener la ubicación: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void abrirCamara() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                Log.d(TAG, "Cámara iniciada");
            } else {
                Log.e(TAG, "No hay aplicación de cámara disponible");
                Toast.makeText(this, "No se puede acceder a la cámara",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al abrir la cámara: " + e.getMessage());
            Toast.makeText(this, "Error al abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirFotosGuardadas() {
        Intent intent = new Intent(MainActivity.this, VerFotosActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        Log.d(TAG, "Imagen capturada exitosamente");

                        Intent intent = new Intent(MainActivity.this, FotosTomadas.class);
                        intent.putExtra("imageBitmap", imageBitmap);
                        intent.putExtra("currentLatitude", currentLatitude);
                        intent.putExtra("currentLongitude", currentLongitude);
                        intent.putExtra("isAceptado", checkBoxAcepto != null &&
                                checkBoxAcepto.isChecked());

                        try {
                            startActivity(intent);
                            Log.d(TAG, "FotosTomadas iniciada");
                        } catch (Exception e) {
                            Log.e(TAG, "Error al iniciar FotosTomadas: " + e.getMessage());
                            Toast.makeText(this, "Error al procesar la foto",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "imageBitmap es null");
                        Toast.makeText(this, "Error: no se pudo obtener la imagen",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "extras es null");
                    Toast.makeText(this, "Error: extras están vacíos",
                            Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en onActivityResult: " + e.getMessage());
            Toast.makeText(this, "Error al procesar el resultado de la cámara",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permisos concedidos");
                obtenerUbicacionYAbrirCamara();
            } else {
                Log.e(TAG, "Permisos denegados");
                Toast.makeText(this, "Se requieren todos los permisos para continuar",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}