package filipesilvestre.homefinder;

import android.app.Application;
import net.danlew.android.joda.JodaTimeAndroid;

public class HomeFinderApplication extends Application {
    @Override
    public void onCreate() {
      super.onCreate();
      JodaTimeAndroid.init(this);
    }
}
