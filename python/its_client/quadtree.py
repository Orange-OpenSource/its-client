from pygeotile.tile import Tile


def lat_lng_to_quad_key(latitude, longitude, level_of_detail, slash=False):
    tile = Tile.for_latitude_longitude(latitude, longitude, level_of_detail)
    if slash:
        quad_tree = f"/{'/'.join(tile.quad_tree)}"
    else:
        quad_tree = tile.quad_tree
    return quad_tree


def is_edgy(direction, q):
    return (
        int(q)
        in {"up": [0, 1], "right": [1, 3], "down": [2, 3], "left": [0, 2]}[direction]
    )


def get_up_or_down(q):
    return str((int(q) + 2) % 4)


def get_right_or_left(q):
    q_as_int = int(q)
    if q_as_int % 2 == 0:
        return str((q_as_int + 1) % 4)
    else:
        return str((q_as_int - 1) % 4)


def get_neighbour(quadtree, direction):
    edge_crossed = False
    result = ""

    for index, q in enumerate(quadtree[::-1]):
        if index == 0 or edge_crossed:
            edge_crossed = is_edgy(direction, q)
            result += {
                "up": get_up_or_down,
                "down": get_up_or_down,
                "right": get_right_or_left,
                "left": get_right_or_left,
            }[direction](q)
        else:
            result += q

    return result[::-1]


# This is the translation of the Java code given by Mathieu on 2019/11/15.
# It works just fine but as long as pygeotile des not give us any error it's probably better to use this lib.
#
#
#
#
# class PixelXY:
#     def __init__(self, pixelX, pixelY):
#         self.pixelX = pixelX
#         self.pixelY = pixelY

# class TileXY:
#     def __init__(self, tileX, tileY):
#         self.tileX = tileX
#         self.tileY = tileY

# def clip(n, minValue, maxValue):
#     return min(max(n, minValue), maxValue)

# def latLngToQuadKey(latitude, longitude, levelOfDetail):
#     return tileXYToQuadKey(pixelXYToTileXY(latLongToPixelXY(latitude, longitude, levelOfDetail)), levelOfDetail)

# def latLongToPixelXY(latitude, longitude, levelOfDetail):
#     latitude = clip(latitude, MIN_LATITUDE, MAX_LATITUDE)
#     longitude = clip(longitude, MIN_LONGITUDE, MAX_LONGITUDE)

#     x = (longitude + 180) /360
#     sinLatitude = math.sin(latitude * math.pi / 180)
#     y = 0.5 - math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * math.pi);

#     mapSize = mapSizeFun(levelOfDetail)
#     pixelX = int (clip(x * mapSize + 0.5, 0, mapSize - 1))
#     pixelY = int (clip(y * mapSize + 0.5, 0, mapSize - 1))
#     return PixelXY(pixelX, pixelY)

# def mapSizeFun(levelOfDetail):
#     return 256 << levelOfDetail

# def pixelXYToTileXY(pixelXY):
#     tileX = int(pixelXY.pixelX / 256)
#     tileY = int(pixelXY.pixelY / 256)
#     return TileXY(tileX, tileY)

# def tileXYToQuadKey(tileXY, levelOfDetail):
#     tileX = tileXY.tileX
#     tileY = tileXY.tileY
#     quadKey = ""
#     for i in range(levelOfDetail, 0, -1):
#         digit = 0
#         mask = 1 << (i - 1)
#         if((tileX & mask) != 0):
#             digit = digit +1
#         if ((tileY & mask) != 0):
#             digit = digit+2
#         quadKey += str(digit)
#     return quadKey
