package gt.edu.umg.gpscamara.BaseDatos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "FotosDB";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_FOTOS = "fotos";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_REMINDER_DATE = "reminder_date";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_ACEPTADO = "aceptado";

    private static final String CREATE_TABLE_FOTOS =
            "CREATE TABLE " + TABLE_FOTOS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_IMAGE + " BLOB NOT NULL, "
                    + COLUMN_NOMBRE + " TEXT NOT NULL, "
                    + COLUMN_LATITUDE + " DOUBLE NOT NULL, "
                    + COLUMN_LONGITUDE + " DOUBLE NOT NULL, "
                    + COLUMN_TIMESTAMP + " INTEGER NOT NULL, "
                    + COLUMN_REMINDER_DATE + " INTEGER, "
                    + COLUMN_ACEPTADO + " INTEGER NOT NULL DEFAULT 0);";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_FOTOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_FOTOS +
                    " ADD COLUMN " + COLUMN_REMINDER_DATE + " INTEGER;");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_FOTOS +
                    " ADD COLUMN " + COLUMN_NOMBRE + " TEXT NOT NULL DEFAULT 'Sin nombre';");
            db.execSQL("ALTER TABLE " + TABLE_FOTOS +
                    " ADD COLUMN " + COLUMN_ACEPTADO + " INTEGER NOT NULL DEFAULT 0;");
        }
    }

    public long insertFoto(byte[] imageBytes, String nombre, double latitude, double longitude,
                           long reminderDate, boolean aceptado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IMAGE, imageBytes);
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
        values.put(COLUMN_REMINDER_DATE, reminderDate);
        values.put(COLUMN_ACEPTADO, aceptado ? 1 : 0);

        long id = db.insert(TABLE_FOTOS, null, values);
        db.close();
        return id;
    }

    public long insertFoto(byte[] imageBytes, String nombre, double latitude, double longitude, long reminderDate) {
        return insertFoto(imageBytes, nombre, latitude, longitude, reminderDate, false);
    }

    public long insertFoto(byte[] imageBytes, String nombre, double latitude, double longitude, boolean aceptado) {
        return insertFoto(imageBytes, nombre, latitude, longitude, 0, aceptado);
    }

    public void deleteFoto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FOTOS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<Foto> getAllFotos() {
        List<Foto> fotos = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_FOTOS + " ORDER BY " + COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
            int imageIndex = cursor.getColumnIndexOrThrow(COLUMN_IMAGE);
            int latitudeIndex = cursor.getColumnIndexOrThrow(COLUMN_LATITUDE);
            int longitudeIndex = cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE);
            int timestampIndex = cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP);
            int reminderDateIndex = cursor.getColumnIndexOrThrow(COLUMN_REMINDER_DATE);
            int nombreIndex = cursor.getColumnIndexOrThrow(COLUMN_NOMBRE);
            int aceptadoIndex = cursor.getColumnIndexOrThrow(COLUMN_ACEPTADO);

            do {
                Foto foto = new Foto();
                foto.setId(cursor.getInt(idIndex));
                foto.setImage(cursor.getBlob(imageIndex));
                foto.setLatitude(cursor.getDouble(latitudeIndex));
                foto.setLongitude(cursor.getDouble(longitudeIndex));
                foto.setTimestamp(cursor.getLong(timestampIndex));
                foto.setReminderDate(cursor.getLong(reminderDateIndex));
                foto.setNombre(cursor.getString(nombreIndex));
                foto.setAceptado(cursor.getInt(aceptadoIndex) == 1);

                fotos.add(foto);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return fotos;
    }

    public Foto getFotoById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FOTOS,
                null,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);

        Foto foto = null;
        if (cursor.moveToFirst()) {
            foto = new Foto();
            foto.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            foto.setImage(cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_IMAGE)));
            foto.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)));
            foto.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)));
            foto.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
            foto.setReminderDate(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_DATE)));
            foto.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)));
            foto.setAceptado(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ACEPTADO)) == 1);
        }
        cursor.close();
        db.close();
        return foto;
    }

    public List<Foto> getFotosWithPendingReminders() {
        List<Foto> fotos = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        String selectQuery = "SELECT * FROM " + TABLE_FOTOS +
                " WHERE " + COLUMN_REMINDER_DATE + " > ? " +
                " ORDER BY " + COLUMN_REMINDER_DATE + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(currentTime)});

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
            int imageIndex = cursor.getColumnIndexOrThrow(COLUMN_IMAGE);
            int latitudeIndex = cursor.getColumnIndexOrThrow(COLUMN_LATITUDE);
            int longitudeIndex = cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE);
            int timestampIndex = cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP);
            int reminderDateIndex = cursor.getColumnIndexOrThrow(COLUMN_REMINDER_DATE);
            int nombreIndex = cursor.getColumnIndexOrThrow(COLUMN_NOMBRE);
            int aceptadoIndex = cursor.getColumnIndexOrThrow(COLUMN_ACEPTADO);

            do {
                Foto foto = new Foto();
                foto.setId(cursor.getInt(idIndex));
                foto.setImage(cursor.getBlob(imageIndex));
                foto.setLatitude(cursor.getDouble(latitudeIndex));
                foto.setLongitude(cursor.getDouble(longitudeIndex));
                foto.setTimestamp(cursor.getLong(timestampIndex));
                foto.setReminderDate(cursor.getLong(reminderDateIndex));
                foto.setNombre(cursor.getString(nombreIndex));
                foto.setAceptado(cursor.getInt(aceptadoIndex) == 1);

                fotos.add(foto);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return fotos;
    }

    public void updateReminderDate(int id, long reminderDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REMINDER_DATE, reminderDate);
        db.update(TABLE_FOTOS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}
