package com.android.oxymeter.room_db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.android.oxymeter.room_db.History.ReadingsDao;
import com.android.oxymeter.room_db.History.ReadingsTable;
import com.android.oxymeter.room_db.History.SessionDao;
import com.android.oxymeter.room_db.History.SessionTable;
import com.android.oxymeter.room_db.Users.UserDao;
import com.android.oxymeter.room_db.Users.UserTable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {UserTable.class, SessionTable.class, ReadingsTable.class}, version = 1, exportSchema = false)
public abstract class OxymeterDatabase extends RoomDatabase {

    public abstract UserDao userDao();

    public abstract SessionDao sessionDao();

    public abstract ReadingsDao readingsDao();

    private static volatile OxymeterDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static OxymeterDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (OxymeterDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            OxymeterDatabase.class, "oxymeter_database")
                            /*.addCallback(sRoomDatabaseCallback)*/
                            .fallbackToDestructiveMigration()//this will clear the database
                            .allowMainThreadQueries()
                            .enableMultiInstanceInvalidation()
                            .build();
                }
            }
        }
        return INSTANCE;
    }


    /**
     * Override the onOpen method to populate the database.
     * For this sample, we clear the database every time it is created or opened.
     * <p>
     * If you want to populate the database only when the database is created for the 1st time,
     * override RoomDatabase.Callback()#onCreate
     */
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                // If you want to start with more words, just add them.
                UserDao dao = INSTANCE.userDao();
                dao.deleteAllSubUsers();

                SessionDao sessionDao = INSTANCE.sessionDao();
                sessionDao.deleteAll();


            });
        }


    };

}
