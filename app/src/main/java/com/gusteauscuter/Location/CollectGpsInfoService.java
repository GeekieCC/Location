package com.gusteauscuter.Location;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CollectGpsInfoService extends Service {
    public CollectGpsInfoService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
