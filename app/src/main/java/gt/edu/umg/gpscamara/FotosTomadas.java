package gt.edu.umg.gpscamara;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gt.edu.umg.gpscamara.FotosGuardadas.BitmapUtils;
import gt.edu.umg.gpscamara.FotosGuardadas.DatabaseHelper;

public class FotosTomadas extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ExecutorService executorService;
    private ImageView imageViewFotoTomada;
    private Button bttnGuardar;
    private Button bttnEliminar;
    private Button bttnTomarFoto;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private double currentLatitude;
    private double currentLongitude;
    private Bitmap currentImageBitmap;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fotostomadas);

        // Inicializar vistas
        imageViewFotoTomada = findViewById(R.id.imageViewFotoTomada);
        bttnGuardar = findViewById(R.id.bttnGuardar);
        bttnEliminar = findViewById(R.id.bttnEliminar);
        bttnTomarFoto = findViewById(R.id.bttnTomarFoto);

        // Inicializar base de datos
        dbHelper = DatabaseHelper.getInstance(getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();

        // Obtener datos del intent
        currentImageBitmap = getIntent().getParcelableExtra("imageBitmap");
        currentLatitude = getIntent().getDoubleExtra("currentLatitude", 0.0);
        currentLongitude = getIntent().getDoubleExtra("currentLongitude", 0.0);

        // Mostrar la imagen
        if (currentImageBitmap != null) {
            imageViewFotoTomada.setImageBitmap(currentImageBitmap);
        }

        // Configurar botones
        bttnGuardar.setOnClickListener(v -> {
            if (currentImageBitmap != null) {
                savePhotoToDatabase(currentImageBitmap, currentLatitude, currentLongitude);
            }
        });

        bttnEliminar.setOnClickListener(v -> {
            // Limpiar la imagen y deshabilitar botones
            imageViewFotoTomada.setImageDrawable(null);
            currentImageBitmap = null;
            bttnGuardar.setEnabled(false);
            Toast.makeText(this, "Foto eliminada", Toast.LENGTH_SHORT).show();
        });

        bttnTomarFoto.setOnClickListener(v -> {
            // Abrir la cámara nuevamente
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        });
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

    private void savePhotoToDatabase(Bitmap imageBitmap, double latitude, double longitude) {
        executorService.execute(() -> {
            try {
                byte[] imageBytes = BitmapUtils.bitmapToByteArray(imageBitmap);
                long id = dbHelper.insertFoto(imageBytes, latitude, longitude);

                runOnUiThread(() -> {
                    if (id != -1) {
                        Toast.makeText(this, "Foto guardada exitosamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error al guardar la foto", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}