package filipesilvestre.homefinder;

import android.location.Location;
import org.apache.commons.math3.ml.clustering.Clusterable;

public class DeviceSnapshot implements Clusterable {

  public Long _id; // for cupboard
  private double lat; // latitude of device
  private double lng; // longitude of device
  private long timestamp; // when this was recorded

  public DeviceSnapshot() {}

  public DeviceSnapshot(Location location, long timestamp) {
    this.lat = location.getLatitude();
    this.lng = location.getLongitude();
    this.timestamp = timestamp;
  }

  public Location getLocation() {
    Location location = new Location("");
    location.setLatitude(lat);
    location.setLongitude(lng);
    return location;
  }

  public double[] getPoint() {
    return new double[]{lat, lng};
  }

  public long getTimestamp() {
    return timestamp;
  }
}
