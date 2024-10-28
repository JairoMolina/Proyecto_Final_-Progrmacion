
package gt.edu.umg.gpscamara;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BaseFotos extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fotos.db";
    private static final String TABLE_IMAGEN = imagenes ;
    private SQLiteDatabase db;
    private int oldVersion;
    private int newVersion;


 