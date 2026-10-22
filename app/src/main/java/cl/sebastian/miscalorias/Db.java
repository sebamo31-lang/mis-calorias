package cl.sebastian.miscalorias;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/** Guarda las comidas en una base SQLite dentro del teléfono. */
public class Db extends SQLiteOpenHelper {

    public static class Entry {
        public long id;
        public String name;
        public int kcal;
        public long ts;
    }

    public static class Food {
        public long id;
        public String name;
        public int kcal;
    }

    public static class DayTotal {
        public String day;
        public int kcal;
    }

    private static Db instance;

    public static synchronized Db get(Context c) {
        if (instance == null) instance = new Db(c.getApplicationContext());
        return instance;
    }

    private Db(Context c) {
        super(c, "calorias.db", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE entries (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "day TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "kcal INTEGER NOT NULL, " +
                "ts INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX idx_day ON entries(day)");
        createFoods(db);
    }

    private static void createFoods(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS foods (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE COLLATE NOCASE, " +
                "kcal INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        if (oldV < 2) createFoods(db);
    }

    // ---- Mis alimentos ----

    /** Guarda un alimento; si ya existe uno con el mismo nombre, actualiza sus calorías. */
    public void saveFood(String name, int kcal) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("kcal", kcal);
        getWritableDatabase().insertWithOnConflict("foods", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void updateFood(long id, String name, int kcal) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("kcal", kcal);
        getWritableDatabase().updateWithOnConflict("foods", v, "id=?",
                new String[]{String.valueOf(id)}, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void deleteFood(long id) {
        getWritableDatabase().delete("foods", "id=?", new String[]{String.valueOf(id)});
    }

    public List<Food> foods(String filter) {
        List<Food> out = new ArrayList<>();
        String f = filter == null ? "" : filter.trim();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, name, kcal FROM foods WHERE name LIKE ? ORDER BY name COLLATE NOCASE",
                new String[]{"%" + f + "%"})) {
            while (c.moveToNext()) {
                Food food = new Food();
                food.id = c.getLong(0);
                food.name = c.getString(1);
                food.kcal = c.getInt(2);
                out.add(food);
            }
        }
        return out;
    }

    public Food findFood(String name) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, name, kcal FROM foods WHERE name = ? COLLATE NOCASE", new String[]{name.trim()})) {
            if (!c.moveToFirst()) return null;
            Food food = new Food();
            food.id = c.getLong(0);
            food.name = c.getString(1);
            food.kcal = c.getInt(2);
            return food;
        }
    }

    public void add(String day, String name, int kcal) {
        ContentValues v = new ContentValues();
        v.put("day", day);
        v.put("name", name);
        v.put("kcal", kcal);
        v.put("ts", System.currentTimeMillis());
        getWritableDatabase().insert("entries", null, v);
    }

    public void delete(long id) {
        getWritableDatabase().delete("entries", "id=?", new String[]{String.valueOf(id)});
    }

    public List<Entry> list(String day) {
        List<Entry> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, name, kcal, ts FROM entries WHERE day=? ORDER BY ts", new String[]{day})) {
            while (c.moveToNext()) {
                Entry e = new Entry();
                e.id = c.getLong(0);
                e.name = c.getString(1);
                e.kcal = c.getInt(2);
                e.ts = c.getLong(3);
                out.add(e);
            }
        }
        return out;
    }

    public int total(String day) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(kcal), 0) FROM entries WHERE day=?", new String[]{day})) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    /** Las comidas que más registras, para agregarlas con un toque. */
    public List<Food> frequent(int limit) {
        List<Food> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT name, kcal, COUNT(*) AS n, MAX(ts) AS last FROM entries " +
                "GROUP BY lower(trim(name)), kcal ORDER BY n DESC, last DESC LIMIT " + limit, null)) {
            while (c.moveToNext()) {
                Food f = new Food();
                f.name = c.getString(0);
                f.kcal = c.getInt(1);
                out.add(f);
            }
        }
        return out;
    }

    public List<DayTotal> history(int limit) {
        List<DayTotal> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT day, SUM(kcal) FROM entries GROUP BY day ORDER BY day DESC LIMIT " + limit, null)) {
            while (c.moveToNext()) {
                DayTotal d = new DayTotal();
                d.day = c.getString(0);
                d.kcal = c.getInt(1);
                out.add(d);
            }
        }
        return out;
    }
}
