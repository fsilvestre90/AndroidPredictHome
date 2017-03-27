package filipesilvestre.homefinder;

import static filipesilvestre.homefinder.utils.UnsubscribeIfPresent.unsubscribe;
import static nl.qbusict.cupboard.CupboardFactory.cupboard;

import android.content.IntentSender;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import nl.qbusict.cupboard.QueryResultIterable;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import weka.classifiers.trees.RandomForest;

public class MainActivity extends BaseActivity {

  private final static int REQUEST_CHECK_SETTINGS = 0;

  @BindView(R.id.device_home_text) TextView mDeviceText;
  @BindView(R.id.predict_button) Button mPredictBtn;

  static SQLiteDatabase db;
  private KMeansPlusPlusClusterer<DeviceSnapshot> clusterer;
  private ReactiveLocationProvider locationProvider;

  private Observable<Location> lastKnownLocationObservable;
  private Observable<Location> locationUpdatesObservable;

  private Subscription lastKnownLocationSubscription;
  private Subscription updatableLocationSubscription;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    clusterer = new KMeansPlusPlusClusterer<>(15, 150);

    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // keep screen on for debug

    setupDatabase();
    setupLocation();
  }

  private void setupDatabase() {
    DatabaseManager dbHelper = new DatabaseManager(this);
    dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 2);
    db = dbHelper.getWritableDatabase();
  }

  private void setupLocation() {
    locationProvider = new ReactiveLocationProvider(getApplicationContext());
    lastKnownLocationObservable = locationProvider.getLastKnownLocation();

    final LocationRequest locationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(50);

    locationUpdatesObservable = locationProvider
        .checkLocationSettings(
            new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build()
        )
        .doOnNext(new Action1<LocationSettingsResult>() {
          @Override
          public void call(LocationSettingsResult locationSettingsResult) {
            Status status = locationSettingsResult.getStatus();
            if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
              try {
                status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
              } catch (IntentSender.SendIntentException ex) {
                Log.e("MainActivity", "Error opening settings activity", ex);
              }
            }
          }
        })
        .flatMap(new Func1<LocationSettingsResult, Observable<Location>>() {
          @Override
          public Observable<Location> call(LocationSettingsResult locationSettingsResult) {
            return locationProvider.getUpdatedLocation(locationRequest);
          }
        });
  }

  public List<DeviceSnapshot> getDeviceLocationHistory() {
    final QueryResultIterable<DeviceSnapshot> iter = cupboard().withDatabase(db)
        .query(DeviceSnapshot.class).query();
    final List<DeviceSnapshot> deviceHistory = new ArrayList<>();
    for (DeviceSnapshot snapshot : iter) {
      deviceHistory.add(snapshot);
    }
    iter.close();

    return deviceHistory;
  }

  public void cluster() {
    if (getDeviceLocationHistory().size() >= 15) {
      List<CentroidCluster<DeviceSnapshot>> clusterResults = clusterer.cluster(getDeviceLocationHistory());

      // put centroid size as key for O(1) search
      Map<Integer, double[]> map = new HashMap<>();
      for (CentroidCluster i : clusterResults) {
        map.put(i.getPoints().size(), i.getCenter().getPoint());
      }
      double[] coord = findLargestClusterPoint(map);

      mDeviceText.setText(String.format("Predicted 'home': { %s, %s }", coord[0], coord[1]));
    } else {
      mDeviceText.setText("Wait a little longer to gather more data...");
    }

  }

  private double[] findLargestClusterPoint(Map<Integer, double[]> map) {
    Comparator<Integer> intLenCmp = new Comparator<Integer>() {
      @Override
      public int compare(Integer o1, Integer o2) {
        return Integer.compare(o1, o2);
      }
    };
    // since keys are the amount of points in a cluster we can compare each and find the largest
    int key = Collections.max(map.keySet(), intLenCmp);
    return map.get(key);
  }

  @OnClick(R.id.predict_button)
  public void predict() {
    cluster();
  }

  public void storeLocation(Location location) {
    Log.d("Log", String.format("Location %s, %s", location.getLatitude(), location.getLongitude()));
    DeviceSnapshot snapshot = new DeviceSnapshot(location,
        Calendar.getInstance().getTimeInMillis());
    cupboard().withDatabase(db).put(snapshot);
  }


  @Override
  protected void onLocationPermissionGranted() {
    lastKnownLocationSubscription = lastKnownLocationObservable
        .doOnNext(new Action1<Location>() {
          @Override
          public void call(Location location) {
            storeLocation(location);
          }
        })
        .subscribe();

    updatableLocationSubscription = locationUpdatesObservable
        .doOnNext(new Action1<Location>() {
          @Override
          public void call(Location location) {
            storeLocation(location);
          }
        })
        .subscribe();
  }

  @Override
  protected void onStop() {
    super.onStop();
    unsubscribe(updatableLocationSubscription);
    unsubscribe(lastKnownLocationSubscription);
  }
}
