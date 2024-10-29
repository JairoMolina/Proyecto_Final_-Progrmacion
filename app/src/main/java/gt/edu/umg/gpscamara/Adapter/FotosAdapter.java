package gt.edu.umg.gpscamara.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.graphics.Bitmap;
import android.widget.Toast;

import gt.edu.umg.gpscamara.Fotos.FotoDetalleActivity;
import gt.edu.umg.gpscamara.FotosGuardadas.BitmapUtils;
import gt.edu.umg.gpscamara.FotosGuardadas.DatabaseHelper;
import gt.edu.umg.gpscamara.FotosGuardadas.Foto;
import gt.edu.umg.gpscamara.R;

public class FotosAdapter extends RecyclerView.Adapter<FotosAdapter.FotoViewHolder> {
    private List<Foto> listaFotos;
    private DatabaseHelper dbHelper;
    private Context context;

    static class FotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvCoordenadas;
        TextView tvFecha;
        TextView tvRecordatorio;
        Button btnEliminar;

        FotoViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewFoto);
            tvCoordenadas = itemView.findViewById(R.id.tvCoordenadas);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvRecordatorio = itemView.findViewById(R.id.tvRecordatorio);
            btnEliminar = itemView.findViewById(R.id.btnEliminarFoto);
        }
    }

    public FotosAdapter(Context context, List<Foto> fotos) {
        this.context = context;
        this.listaFotos = fotos;
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public void actualizarFotos(List<Foto> nuevasFotos) {
        this.listaFotos = nuevasFotos;
        notifyDataSetChanged();
    }

    @Override
    public FotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_foto, parent, false);
        return new FotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FotoViewHolder holder, int position) {
        Foto foto = listaFotos.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        // Convertir bytes a Bitmap
        Bitmap bitmap = BitmapUtils.byteArrayToBitmap(foto.getImage());
        holder.imageView.setImageBitmap(bitmap);

        // Mostrar coordenadas
        holder.tvCoordenadas.setText(String.format(Locale.getDefault(),
                "Ubicación:\nLatitud: %.4f\nLongitud: %.4f",
                foto.getLatitude(), foto.getLongitude()));

        // Mostrar fecha de captura
        String fechaCaptura = sdf.format(new Date(foto.getTimestamp()));
        holder.tvFecha.setText("Fecha: " + fechaCaptura);

        // Mostrar recordatorio si existe
        if (foto.getReminderDate() > 0) {
            String fechaRecordatorio = sdf.format(new Date(foto.getReminderDate()));
            holder.tvRecordatorio.setVisibility(View.VISIBLE);
            holder.tvRecordatorio.setText("Recordatorio: " + fechaRecordatorio);
        } else {
            holder.tvRecordatorio.setVisibility(View.GONE);
        }

        // Configurar botón eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Eliminar foto")
                    .setMessage("¿Estás seguro de que deseas eliminar esta foto?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        dbHelper.deleteFoto(foto.getId());
                        listaFotos.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, listaFotos.size());
                        Toast.makeText(context, "Foto eliminada", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // Configurar click en la imagen para ver detalles
        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FotoDetalleActivity.class);
            intent.putExtra("photoId", foto.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaFotos.size();
    }
}