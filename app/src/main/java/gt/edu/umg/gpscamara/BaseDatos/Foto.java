package gt.edu.umg.gpscamara.BaseDatos;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Foto  {
    private int id;
    private byte[] image;
    private double latitude;
    private double longitude;
    private long timestamp;
    private long reminderDate; // Nuevo campo para la fecha del recordatorio
    private String nombre; // Nuevo atributo
    private boolean aceptado; //

    public Foto() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public long getReminderDate() { return reminderDate; }
    public void setReminderDate(long reminderDate) { this.reminderDate = reminderDate; }

    public String getNombre() {
        return nombre; // Nuevo getter
    }

    public void setNombre(String nombre) {
        this.nombre = nombre; // Nuevo setter
    }

    public boolean isAceptado() {
        return aceptado; // Nuevo getter
    }

    public void setAceptado(boolean aceptado) {
        this.aceptado = aceptado; // Nuevo setter
    }

    // Método útil para verificar si tiene un recordatorio pendiente
    public boolean hasPendingReminder() {
        return reminderDate > System.currentTimeMillis();
    }

    // Método útil para obtener la fecha formateada del recordatorio
    public String getFormattedReminderDate() {
        if (reminderDate <= 0) return "Sin recordatorio";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(reminderDate));
    }
}