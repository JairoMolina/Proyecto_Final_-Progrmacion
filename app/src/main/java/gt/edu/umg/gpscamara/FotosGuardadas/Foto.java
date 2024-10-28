package gt.edu.umg.gpscamara.FotosGuardadas;



public class Foto {
    private int id;
    private byte[] image;
    private double latitude;
    private double longitude;
    private long timestamp;

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
}