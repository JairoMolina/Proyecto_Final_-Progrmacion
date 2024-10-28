package gt.edu.umg.gpscamara;

import static android.content.Intent.getIntent;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FotosTomadasActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fotostomadas);

        // Obtener la imagen capturada del Intent
        Bitmap imageBitmap = (Bitmap) getIntent().getParcelableExtra("imageBitmap");
        double currentLatitude = getIntent().getDoubleExtra("currentLatitude", 0.0);
        double currentLongitude = getIntent().getDoubleExtra("currentLongitude", 0.0);

        // Mostrar la imagen en el ImageView
        ImageView imageViewFotoTomada = findViewById(R.id.imageViewFotoTomada);
        imageViewFotoTomada.setImageBitmap(imageBitmap);

        // Mostrar mensaje con la ubicación
        String mensaje = "Foto guardada en:\nLatitud: " + currentLatitude +
                "\nLongitud: " + currentLongitude;
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }
}