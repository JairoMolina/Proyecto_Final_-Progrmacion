package gt.edu.umg.gpscamara.Fotos;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import gt.edu.umg.gpscamara.FotosGuardadas.DatabaseHelper;
import gt.edu.umg.gpscamara.FotosGuardadas.Foto;
import gt.edu.umg.gpscamara.R;

public class FotoDetalleActivity extends AppCompatActivity {
    private static final String TAG = "FotoDetalleActivity";

    private ImageView imageViewFoto;
    private TextView tvFecha;
    private TextView tvUbicacion;
    private TextView tvRecordatorio;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "Iniciando onCreate");
            setContentView(R.layout.activity_foto_detalle);

            // Inicializar vistas
            initializeViews();

            dbHelper = DatabaseHelper.getInstance(this);

            // Obtener el ID de la foto
            long photoId = getIntent().getLongExtra("photoId", -1);
            Log.d(TAG, "ID de foto recibido: " + photoId);

            if (photoId != -1) {
                cargarFoto(photoId);
            } else {
                Log.e(TAG, "No se recibió un ID de foto válido");
                Toast.makeText(this, "Error: ID de foto no válido", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error al cargar los detalles", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            imageViewFoto = findViewById(R.id.imageViewFotoDetalle);
            if (imageViewFoto == null) throw new NullPointerException("imageViewFotoDetalle no encontrado");

            tvFecha = findViewById(R.id.tvFecha);
            if (tvFecha == null) throw new NullPointerException("tvFecha no encontrado");

            tvUbicacion = findViewById(R.id.tvUbicacion);
            if (tvUbicacion == null) throw new NullPointerException("tvUbicacion no encontrado");

            tvRecordatorio = findViewById(R.id.tvRecordatorio);
            if (tvRecordatorio == null) throw new NullPointerException("tvRecordatorio no encontrado");

            Log.d(TAG, "Vistas inicializadas correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error al inicializar vistas: " + e.getMessage(), e);
            throw e;
        }
    }

    private void cargarFoto(long photoId) {
        try {
            Log.d(TAG, "Cargando foto con ID: " + photoId);
            Foto foto = dbHelper.getFotoById((int) photoId);

            if (foto != null) {
                // Mostrar la imagen
                byte[] imageBytes = foto.getImage();
                if (imageBytes != null && imageBytes.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    if (bitmap != null) {
                        imageViewFoto.setImageBitmap(bitmap);
                        Log.d(TAG, "Imagen cargada correctamente");
                    } else {
                        Log.e(TAG, "Error al decodificar la imagen");
                        imageViewFoto.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                }

                // Formatear y mostrar la fecha de captura
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                String fechaCaptura = sdf.format(new Date(foto.getTimestamp()));
                tvFecha.setText("Fecha de captura: " + fechaCaptura);
                Log.d(TAG, "Fecha de captura: " + fechaCaptura);

                // Mostrar ubicación
                String ubicacion = String.format(Locale.getDefault(),
                        "Ubicación:\nLatitud: %.6f\nLongitud: %.6f",
                        foto.getLatitude(),
                        foto.getLongitude());
                tvUbicacion.setText(ubicacion);
                Log.d(TAG, "Ubicación cargada: " + ubicacion);

                // Mostrar recordatorio si existe
                if (foto.getReminderDate() > 0) {
                    String fechaRecordatorio = sdf.format(new Date(foto.getReminderDate()));
                    tvRecordatorio.setText("Recordatorio programado para: " + fechaRecordatorio);
                    tvRecordatorio.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    Log.d(TAG, "Recordatorio: " + fechaRecordatorio);
                } else {
                    tvRecordatorio.setText("Sin recordatorio programado");
                    tvRecordatorio.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    Log.d(TAG, "Sin recordatorio");
                }
            } else {
                Log.e(TAG, "No se encontró la foto con ID: " + photoId);
                Toast.makeText(this, "Error: Foto no encontrada", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al cargar la foto: " + e.getMessage(), e);
            Toast.makeText(this, "Error al cargar los detalles de la foto", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            // Limpiar recursos si es necesario
        } catch (Exception e) {
            Log.e(TAG, "Error en onDestroy: " + e.getMessage(), e);
        }
    }
}