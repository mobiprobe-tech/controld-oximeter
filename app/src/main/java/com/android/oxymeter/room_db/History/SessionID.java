package com.android.oxymeter.room_db.History;

import androidx.room.ColumnInfo;

public class SessionID {

    @ColumnInfo(name = "id")
    public long id;

    @ColumnInfo(name = "session_id")
    String mSessionID;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getmSessionID() {
        return mSessionID;
    }

    public void setmSessionID(String mSessionID) {
        this.mSessionID = mSessionID;
    }
}
