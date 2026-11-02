package cl.saba.operaciones.data

import kotlinx.coroutines.withContext

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.saba.operaciones.domain.Parking
import cl.saba.operaciones.domain.ParkingCatalog
import kotlinx.coroutines.Dispatchers
import org.json.JSONArray

class ParkingRepository(private val context: Context) : SQLiteOpenHelper(context, "saba.db", null, 1){
    override fun onConfigure(db: SQLiteDatabase){
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase){
        db.execSQL("CREATE TABLE parking (id TEXT PRIMARY KEY, name TEXT NOT NULL, commune TEXT NOT NULL, spaces INTEGER NOT NULL CHECK(spaces > 0))")
        db.execSQL("CREATE TABLE selection (id INTEGER PRIMARY KEY CHECK(id = 1), parking_id TEXT NOT NULL REFERENCES parking(id))")
        val json = context.assets.open("parkings.json").bufferedReader().use{ it.readText() }
        val parkings = JSONArray(json)
        db.compileStatement("INSERT INTO parking (id, name, commune, spaces) VALUES (?, ?, ?, ?)").use{ statement ->
            for (index in 0 until parkings.length()){
                val parking = parkings.getJSONObject(index)
                statement.bindString(1, parking.getString("id"))
                statement.bindString(2, parking.getString("name"))
                statement.bindString(3, parking.getString("commune"))
                statement.bindLong(4, parking.getLong("spaces"))
                statement.executeInsert()
            }
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int){
        error("No hay una migración de $oldVersion a $newVersion.")
    }

    suspend fun load(): ParkingCatalog = withContext(Dispatchers.IO){
        val parkings = mutableListOf<Parking>()
        readableDatabase.rawQuery("SELECT id, name, commune, spaces FROM parking ORDER BY rowid", null).use{ cursor ->
            while (cursor.moveToNext()){
                parkings.add(Parking(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3)))
            }
        }
        val selectedId = readableDatabase.rawQuery("SELECT parking_id FROM selection WHERE id = 1", null).use{ cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
        ParkingCatalog(parkings, selectedId)
    }

    suspend fun select(parkingId: String){
        withContext(Dispatchers.IO){
            val values = ContentValues()
            values.put("id", 1)
            values.put("parking_id", parkingId)
            writableDatabase.insertWithOnConflict("selection", null, values, SQLiteDatabase.CONFLICT_REPLACE).also{ result ->
                check(result != -1L)
            }
        }
    }
}
