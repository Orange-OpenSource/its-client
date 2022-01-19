import random


class GeoPosition:
    def __init__(self, latitude: float, longitude: float):
        self.count = 0
        self.lat = latitude
        self.lon = longitude

    def get_current_position(self):
        return self.lon, self.lat

    def get_current_value(self):
        # stub
        self.count += 1
        if self.count % 5 == 0:
            lon_drift = round(random.uniform(-0.000002, 0.000002), 6)
            lat_drift = round(random.uniform(-0.000002, 0.000002), 6)
            self.count = 0
        else:
            lon_drift = 0
            lat_drift = 0
        lon, lat = self.get_current_position()
        lon = lon + lon_drift
        lat = lat + lat_drift
        speed = 0.103
        alt = 131.693
        heading = 130.7275
        return lon, lat, speed, alt, heading
