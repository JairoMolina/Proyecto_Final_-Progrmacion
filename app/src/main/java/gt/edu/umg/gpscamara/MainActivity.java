package gt.edu.umg.gpscamara;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;


public class MainActivity extends AppCompatActivity {

    private Button bttnCamara1;
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

        //
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        bttnCamara1 = findViewById(R.id.bttnCamara1);

        bttnCamara1.setOnClickListener(v -> {
            verificarPermisos();
        });
    }

    private void verificarPermisos() {

        // Verificar permisos de cámara y ubicación
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
//
    private void obtenerUbicacionYAbrirCamara() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();

                        // Mostrar la ubicación (opcional)
                        Toast.makeText(MainActivity.this,
                                "Lat: " + currentLatitude + ", Long: " + currentLongitude,
                                Toast.LENGTH_SHORT).show();

                        // Abrir la cámara
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

            guardarFotoYUbicacion(data);
        }
    }
   //
    private void guardarFotoYUbicacion(Intent data) {
        Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

        // Crear un Intent para abrir activity_fotostomadas
        Intent intent = new Intent(MainActivity.this, FotosTomadasActivity.class);
        intent.putExtra("imageBitmap", imageBitmap);
        intent.putExtra("currentLatitude", currentLatitude);
        intent.putExtra("currentLongitude", currentLongitude);
        startActivity(intent);
    }
    //
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