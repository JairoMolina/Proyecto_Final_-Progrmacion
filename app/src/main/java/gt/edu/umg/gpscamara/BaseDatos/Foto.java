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
    private long reminderDate;
    private String nombre;
    private boolean aceptado;

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
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isAceptado() {
        return aceptado;
    }

    public void setAceptado(boolean aceptado) {
        this.aceptado = aceptado;
    }

    public boolean hasPendingReminder() {
        return reminderDate > System.currentTimeMillis();
    }

    public String getFormattedReminderDate() {
        if (reminderDate <= 0) return "Sin recordatorio";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(reminderDate));
    }
}