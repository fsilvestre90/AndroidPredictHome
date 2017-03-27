package filipesilvestre.homefinder;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseManager extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "device_location.db";
  private static final int DATABASE_VERSION = 1;

  static {
    // register our models
    cupboard().register(DeviceSnapshot.class);
  }

  public DatabaseManager(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    // this will ensure that all tables are created
    cupboard().withDatabase(db).createTables();
  }


  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // this will upgrade tables, adding columns and new tables.
    cupboard().withDatabase(db).upgradeTables();
  }

}
