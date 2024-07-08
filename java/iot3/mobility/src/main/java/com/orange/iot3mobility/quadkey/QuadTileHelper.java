package com.orange.iot3mobility.quadkey;

import java.util.ArrayList;

public class QuadTileHelper {

    private static final double EarthRadius = 6378137;
    private static final double MinLatitude = -85.05112878;
    private static final double MaxLatitude = 85.05112878;
    private static final double MinLongitude = -180;
    private static final double MaxLongitude = 180;

    /// <summary>
    /// Clips a number to the specified minimum and maximum values.
    /// </summary>
    /// <param name="n">The number to clip.</param>
    /// <param name="minValue">Minimum allowable value.</param>
    /// <param name="maxValue">Maximum allowable value.</param>
    /// <returns>The clipped value.</returns>
    private static double clip(double n, double minValue, double maxValue) {
        return Math.min(Math.max(n, minValue), maxValue);
    }

    /// <summary>
    /// Determines the map width and height (in pixels) at a specified level
    /// of detail.
    /// </summary>
    /// <param name="levelOfDetail">Level of detail, from 1 (lowest detail)
    /// to 23 (highest detail).</param>
    /// <returns>The map width and height in pixels.</returns>
    private static int mapSize(int levelOfDetail) {
        return 256 << levelOfDetail;
    }

    /// <summary>
    /// Determines the ground resolution (in meters per pixel) at a specified
    /// latitude and level of detail.
    /// </summary>
    /// <param name="latitude">Latitude (in degrees) at which to measure the
    /// ground resolution.</param>
    /// <param name="levelOfDetail">Level of detail, from 1 (lowest detail)
    /// to 23 (highest detail).</param>
    /// <returns>The ground resolution, in meters per pixel.</returns>
    private static double groundResolution(double latitude, int levelOfDetail) {
        latitude = clip(latitude, MinLatitude, MaxLatitude);
        return Math.cos(latitude * Math.PI / 180) * 2 * Math.PI * EarthRadius / mapSize(levelOfDetail);
    }

    /// <summary>
    /// Determines the map scale at a specified latitude, level of detail,
    /// and screen resolution.
    /// </summary>
    /// <param name="latitude">Latitude (in degrees) at which to measure the
    /// map scale.</param>
    /// <param name="levelOfDetail">Level of detail, from 1 (lowest detail)
    /// to 23 (highest detail).</param>
    /// <param name="screenDpi">Resolution of the screen, in dots per inch.</param>
    /// <returns>The map scale, expressed as the denominator N of the ratio 1 : N.</returns>
    public static double mapScale(double latitude, int levelOfDetail, int screenDpi) {
        return groundResolution(latitude, levelOfDetail) * screenDpi / 0.0254;
    }

    /// <summary>
    /// Converts a point from latitude/longitude WGS-84 coordinates (in degrees)
    /// into pixel XY coordinates at a specified level of detail.
    /// </summary>
    /// <param name="latitude">Latitude of the point, in degrees.</param>
    /// <param name="longitude">Longitude of the point, in degrees.</param>
    /// <param name="levelOfDetail">Level of detail, from 1 (lowest detail)
    /// to 23 (highest detail).</param>
    /// <param name="pixelX">Output parameter receiving the X coordinate in pixels.</param>
    /// <param name="pixelY">Output parameter receiving the Y coordinate in pixels.</param>
    public static PixelXY latLongToPixelXY(double latitude, double longitude, int levelOfDetail) {
        latitude = clip(latitude, MinLatitude, MaxLatitude);
        longitude = clip(longitude, MinLongitude, MaxLongitude);

        double x = (longitude + 180) / 360;
        double sinLatitude = Math.sin(latitude * Math.PI / 180);
        double y = 0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI);

        int mapSize = mapSize(levelOfDetail);
        int pixelX = (int) clip(x * mapSize + 0.5, 0, mapSize - 1);
        int pixelY = (int) clip(y * mapSize + 0.5, 0, mapSize - 1);
        return new PixelXY(pixelX, pixelY);
    }

    /// <summary>
    /// Converts a pixel from pixel XY coordinates at a specified level of detail
    /// into latitude/longitude WGS-84 coordinates (in degrees).
    /// </summary>
    /// <param name="pixelX">X coordinate of the point, in pixels.</param>
    /// <param name="pixelY">Y coordinates of the point, in pixels.</param>
    /// <param name="levelOfDetail">Level of detail, from 1 (lowest detail)
    /// to 23 (highest detail).</param>
    /// <param name="latitude">Output parameter receiving the latitude in degrees.</param>
    /// <param name="longitude">Output parameter receiving the longitude in degrees.</param>
    public static LatLng pixelXYToLatLong(PixelXY pixelXY, int levelOfDetail) {
        int pixelX = pixelXY.getPixelX();
        int pixelY = pixelXY.getPixelY();
        double mapSize = mapSize(levelOfDetail);
        double x = (clip(pixelX, 0, mapSize - 1) / mapSize) - 0.5;
        double y = 0.5 - (clip(pixelY, 0, mapSize - 1) / mapSize);

        double latitude = 90 - 360 * Math.atan(Math.exp(-y * 2 * Math.PI)) / Math.PI;
        double longitude = 360 * x;
        return new LatLng(latitude, longitude);
    }

    /// <summary>
    /// Converts pixel XY coordinates into tile XY coordinates of the tile containing
    /// the specified pixel.
    /// </summary>
    /// <param name="pixelX">Pixel X coordinate.</param>
    /// <param name="pixelY">Pixel Y coordinate.</param>
    /// <param name="tileX">Output parameter receiving the tile X coordinate.</param>
    /// <param name="tileY">Output parameter receiving the tile Y coordinate.</param>
    public static TileXY pixelXYToTileXY(PixelXY pixelXY) {
        int tileX = pixelXY.getPixelX() / 256;
        int tileY = pixelXY.getPixelY() / 256;
        return new TileXY(tileX, tileY);
    }

    /// <summary>
    /// Converts tile XY coordinates into pixel XY coordinates of the upper-left pixel
    /// of the specified tile.
    /// </summary>
    /// <param name="tileX">Tile X coordinate.</param>
    /// <param name="tileY">Tile Y coordinate.</param>
    /// <param name="pixelX">Output parameter receiving the pixel X coordinate.</param>
    /// <param name="pixelY">Output parameter receiving the pixel Y coordinate.</param>
    public static PixelXY tileXYToPixelXYUpLeft(TileXY tileXY) {
        int pixelX = tileXY.getTileX() * 256;
        int pixelY = tileXY.getTileY() * 256;
        return new PixelXY(pixelX, pixelY);
    }

    /// <summary>
    /// Converts tile XY coordinates into pixel XY coordinates of the lower-right pixel
    /// of the specified tile.
    /// </summary>
    /// <param name="tileX">Tile X coordinate.</param>
    /// <param name="tileY">Tile Y coordinate.</param>
    /// <param name="pixelX">Output parameter receiving the pixel X coordinate.</param>
    /// <param name="pixelY">Output parameter receiving the pixel Y coordinate.</param>
    public static PixelXY tileXYToPixelXYLowRight(TileXY tileXY) {
        int pixelX = tileXY.getTileX() * 256 + 256;
        int pixelY = tileXY.getTileY() * 256 + 256;
        return new PixelXY(pixelX, pixelY);
    }

    /// <summary>
    /// Converts tile XY coordinates into pixel XY coordinates of the lower-left pixel
    /// of the specified tile.
    /// </summary>
    /// <param name="tileX">Tile X coordinate.</param>
    /// <param name="tileY">Tile Y coordinate.</param>
    /// <param name="pixelX">Output parameter receiving the pixel X coordinate.</param>
    /// <param name="pixelY">Output parameter receiving the pixel Y coordinate.</param>
    public static PixelXY tileXYToPixelXYLowLeft(TileXY tileXY) {
        int pixelX = tileXY.getTileX() * 256;
        int pixelY = tileXY.getTileY() * 256 + 256;
        return new PixelXY(pixelX, pixelY);
    }

    /// <summary>
    /// Converts tile XY coordinates into pixel XY coordinates of the upper-right pixel
    /// of the specified tile.
    /// </summary>
    /// <param name="tileX">Tile X coordinate.</param>
    /// <param name="tileY">Tile Y coordinate.</param>
    /// <param name="pixelX">Output parameter receiving the pixel X coordinate.</param>
    /// <param name="pixelY">Output parameter receiving the pixel Y coordinate.</param>
    public static PixelXY tileXYToPixelXYUpRight(TileXY tileXY) {
        int pixelX = tileXY.getTileX() * 256 + 256;
        int pixelY = tileXY.getTileY() * 256;
        return new PixelXY(pixelX, pixelY);
    }

    /// <summary>
    /// Converts tile XY coordinates into a QuadKey at a specified level of detail.
    /// </summary>
    /// <param name="tileX">Tile X coordinate.</param>
    /// <param name="tileY">Tile Y coordinate.</param>
    /// <param name="levelOfDetail">Level of detail, from 1 (lowest detail)
    /// to 23 (highest detail).</param>
    /// <returns>A string containing the QuadKey.</returns>
    public static String tileXYToQuadKey(TileXY tileXY, int levelOfDetail) {
        int tileX = tileXY.getTileX();
        int tileY = tileXY.getTileY();
        StringBuilder quadKey = new StringBuilder();
        for (int i = levelOfDetail; i > 0; i--)
        {
            char digit = '0';
            int mask = 1 << (i - 1);
            if ((tileX & mask) != 0)
            {
                digit++;
            }
            if ((tileY & mask) != 0)
            {
                digit++;
                digit++;
            }
            quadKey.append(digit);
        }
        return quadKey.toString();
    }

    /// <summary>
    /// Converts a point from latitude/longitude WGS-84 coordinates (in degrees)
    /// into a QuadKey at a specified level of detail.
    /// </summary>
    /// <param name="latitude">Latitude of the point, in degrees.</param>
    /// <param name="longitude">Longitude of the point, in degrees.</param>
    /// <param name="levelOfDetail">Level of detail, from 1 (lowest detail)
    /// to 23 (highest detail).</param>
    /// <returns>A string containing the QuadKey.</returns>
    public static String latLngToQuadKey(double latitude, double longitude, int levelOfDetail)
    {
        return tileXYToQuadKey(pixelXYToTileXY(latLongToPixelXY(latitude, longitude, levelOfDetail)), levelOfDetail);
    }

    /// <summary>
    /// Converts a QuadKey into tile XY coordinates.
    /// </summary>
    /// <param name="quadKey">QuadKey of the tile.</param>
    /// <param name="tileX">Output parameter receiving the tile X coordinate.</param>
    /// <param name="tileY">Output parameter receiving the tile Y coordinate.</param>
    /// <param name="levelOfDetail">Output parameter receiving the level of detail.</param>
    public static TileXY quadKeyToTileXY(String quadKey) {
        int tileX = 0;
        int tileY = 0;
        int levelOfDetail = quadKey.length();
        for (int i = levelOfDetail; i > 0; i--)
        {
            int mask = 1 << (i - 1);
            int value = Character.getNumericValue(quadKey.charAt(levelOfDetail - i));
            switch (value)
            {
                case 0:
                    break;

                case 1:
                    tileX |= mask;
                    break;

                case 2:
                    tileY |= mask;
                    break;

                case 3:
                    tileX |= mask;
                    tileY |= mask;
                    break;

                default:
                    break;
            }
        }
        return new TileXY(tileX, tileY);
    }

    public static ArrayList<String> getNeighborQuadKeys(String quadKey) {
        ArrayList<TileXY> neighborTilesXY = new ArrayList<>();
        TileXY tileXY = quadKeyToTileXY(quadKey);
        neighborTilesXY.add(new TileXY(tileXY.getTileX() - 1, tileXY.getTileY() - 1));
        neighborTilesXY.add(new TileXY(tileXY.getTileX(), tileXY.getTileY() - 1));
        neighborTilesXY.add(new TileXY(tileXY.getTileX() + 1, tileXY.getTileY() - 1));
        neighborTilesXY.add(new TileXY(tileXY.getTileX() - 1, tileXY.getTileY()));
        neighborTilesXY.add(new TileXY(tileXY.getTileX() + 1, tileXY.getTileY()));
        neighborTilesXY.add(new TileXY(tileXY.getTileX() - 1, tileXY.getTileY() + 1));
        neighborTilesXY.add(new TileXY(tileXY.getTileX(), tileXY.getTileY() + 1));
        neighborTilesXY.add(new TileXY(tileXY.getTileX() + 1, tileXY.getTileY() + 1));

        ArrayList<String> neighborTilesKey = new ArrayList<>();
        for(TileXY tile: neighborTilesXY){
            neighborTilesKey.add(tileXYToQuadKey(tile, quadKey.length()));
        }

        return neighborTilesKey;
    }

    public static ArrayList<String> substractKeys(ArrayList<String> targetKeys,
                                                  ArrayList<String> extractKeys) {
        ArrayList<String> resultKeys = new ArrayList<>();
        for(String targetKey: targetKeys) {
            boolean keepTargetKey = true;
            for(String extractKey: extractKeys) {
                if(extractKey.startsWith(targetKey)) {
                    keepTargetKey = false;
                    if(!extractKey.equals(targetKey)) {
                        // split into four subkeys (zoom z+1)
                        ArrayList<String> subkeys = new ArrayList<>();
                        subkeys.add(targetKey.concat("0"));
                        subkeys.add(targetKey.concat("1"));
                        subkeys.add(targetKey.concat("2"));
                        subkeys.add(targetKey.concat("3"));
                        ArrayList<String> substractedKeys = substractKeys(subkeys, extractKeys);
                        for(String substractedKey: substractedKeys) {
                            if(!resultKeys.contains(substractedKey)) resultKeys.add(substractedKey);
                        }
                    }
                }
            }
            if(keepTargetKey && !resultKeys.contains(targetKey)) resultKeys.add(targetKey);
        }
        return resultKeys;
    }

    public static String quadKeyToQuadTopic(String quadKey) {
        String quadTopic = "";
        for(int i = 0; i < quadKey.length(); i++){
            quadTopic = quadTopic.concat("/"+quadKey.charAt(i));
        }
        return quadTopic;
    }

}
