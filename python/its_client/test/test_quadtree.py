import unittest
from its_client import quadtree
from its_client.quadtree import (
    is_edgy,
    get_up_or_down,
    get_right_or_left,
    get_neighbour,
)


class TestQuadTree(unittest.TestCase):
    def setUp(self):
        self.latitude = 486263556
        self.longitude = 22492123

    def tearDown(self):
        self.dataframe = None

    def test_lat_lng_to_quad_key_path(self):
        self.assertEqual(
            "120220011203",
            quadtree.lat_lng_to_quad_key(
                latitude=self.latitude / 10000000,
                longitude=self.longitude / 10000000,
                level_of_detail=12,
            ),
        )

    def test_lat_lng_to_quad_key_zero(self):
        data = [83689428, -143165555]
        self.assertEqual(
            "033321211101",
            quadtree.lat_lng_to_quad_key(
                latitude=data[0] / 10000000,
                longitude=data[1] / 10000000,
                level_of_detail=12,
            ),
        )

    def test_lat_lng_to_quad_key_path_without_slash(self):
        self.assertEqual(
            "120220011203",
            quadtree.lat_lng_to_quad_key(
                latitude=self.latitude / 10000000,
                longitude=self.longitude / 10000000,
                level_of_detail=12,
                slash=False,
            ),
        )

    def test_lat_lng_to_quad_key_path_with_slash(self):
        self.assertEqual(
            "/1/2/0/2/2/0/0/1/1/2/0/3",
            quadtree.lat_lng_to_quad_key(
                latitude=self.latitude / 10000000,
                longitude=self.longitude / 10000000,
                level_of_detail=12,
                slash=True,
            ),
        )

    def test_lat_lng_to_quad_key_path_with_a_level_of_detail(self):
        self.assertEqual(
            "120220011203100323112320",
            quadtree.lat_lng_to_quad_key(
                latitude=self.latitude / 10000000,
                longitude=self.longitude / 10000000,
                level_of_detail=24,
            ),
        )

    def test_lat_lng_to_quad_key_path_with_a_level_of_detail_and_without_slash(self):
        self.assertEqual(
            "120220011203100323",
            quadtree.lat_lng_to_quad_key(
                latitude=self.latitude / 10000000,
                longitude=self.longitude / 10000000,
                level_of_detail=18,
                slash=False,
            ),
        )

    def test_lat_lng_to_quad_key_path_with_a_level_of_detail_and_with_slash(self):
        self.assertEqual(
            "/1/2/0/2/2/0/0/1/1/2/0/3/1/0/0/3",
            quadtree.lat_lng_to_quad_key(
                latitude=self.latitude / 10000000,
                longitude=self.longitude / 10000000,
                level_of_detail=16,
                slash=True,
            ),
        )


class TestNeighbor(unittest.TestCase):
    """
    |-------------------------------|-------------------------------|
    |000    |001    |010    |011    |100    |101    |110    |111    |
    |       |       |       |       |       |       |       |       |
    |-------------------------------|-------------------------------|
    |002    |003    |012    |013    |102    |103    |112    |113    |
    |       |       |       |       |       |       |       |       |
    |-------------------------------|-------------------------------|
    |020    |021    |030    |031    |120    |121    |130    |131    |
    |       |       |       |   ^   |   ^   |       |       |       |
    |-------------------------------|-------------------------------|
    |022    |023    |032    |033    |122    |123    |132    |133    |
    |       |       |       |   *   |   *   |       |       |       |
    |-------------------------------|-------------------------------|
    |200    |201    |210    |211    |300    |301    |310    |311    |
    |       |       |       |   *   |   *   |       |       |       |
    |-------------------------------|-------------------------------|
    |202    |203    |212    |213    |302    |303    |312    |313    |
    |       |       |       |       |       |       |       |       |
    |-------------------------------|-------------------------------|
    |220    |221    |230    |231    |320    |321    |330    |331    |
    |       |       |       |       |       |       |       |       |
    |-------------------------------|-------------------------------|
    |222    |223    |232    |233    |322    |323    |332    |333    |
    |       |       |       |       |       |       |       |       |
    |-------------------------------|-------------------------------|

    Neighbourhood tests uses the 4 central quadkeys of this 3 level deep quadkey list
    """

    def test_up_edgyness(self):
        self.assertTrue(is_edgy("up", 0))
        self.assertTrue(is_edgy("up", 1))
        self.assertFalse(is_edgy("up", 2))
        self.assertFalse(is_edgy("up", 3))

    def test_right_edgyness(self):
        self.assertFalse(is_edgy("right", 0))
        self.assertTrue(is_edgy("right", 1))
        self.assertFalse(is_edgy("right", 2))
        self.assertTrue(is_edgy("right", 3))

    def test_down_edgyness(self):
        self.assertFalse(is_edgy("down", 0))
        self.assertFalse(is_edgy("down", 1))
        self.assertTrue(is_edgy("down", 2))
        self.assertTrue(is_edgy("down", 3))

    def test_left_edgyness(self):
        self.assertTrue(is_edgy("left", 0))
        self.assertFalse(is_edgy("left", 1))
        self.assertTrue(is_edgy("left", 2))
        self.assertFalse(is_edgy("left", 3))

    def test_get_up_quadkey_digit(self):
        self.assertEqual(get_up_or_down("0"), "2")
        self.assertEqual(get_up_or_down("1"), "3")
        self.assertEqual(get_up_or_down("2"), "0")
        self.assertEqual(get_up_or_down("3"), "1")

    def test_get_down_quadkey_digit(self):
        self.assertEqual(get_up_or_down("0"), "2")
        self.assertEqual(get_up_or_down("1"), "3")
        self.assertEqual(get_up_or_down("2"), "0")
        self.assertEqual(get_up_or_down("3"), "1")

    def test_get_right_quadkey_digit(self):
        self.assertEqual(get_right_or_left("0"), "1")
        self.assertEqual(get_right_or_left("1"), "0")
        self.assertEqual(get_right_or_left("2"), "3")
        self.assertEqual(get_right_or_left("3"), "2")

    def test_get_left_quadkey_digit(self):
        self.assertEqual(get_right_or_left("0"), "1")
        self.assertEqual(get_right_or_left("1"), "0")
        self.assertEqual(get_right_or_left("2"), "3")
        self.assertEqual(get_right_or_left("3"), "2")

    def test_get_up(self):
        self.assertEqual(get_neighbour("033", "up"), "031")
        self.assertEqual(get_neighbour("122", "up"), "120")
        self.assertEqual(get_neighbour("300", "up"), "122")
        self.assertEqual(get_neighbour("211", "up"), "033")

    def test_get_right(self):
        self.assertEqual(get_neighbour("033", "right"), "122")
        self.assertEqual(get_neighbour("122", "right"), "123")
        self.assertEqual(get_neighbour("300", "right"), "301")
        self.assertEqual(get_neighbour("211", "right"), "300")

    def test_get_down(self):
        self.assertEqual(get_neighbour("033", "down"), "211")
        self.assertEqual(get_neighbour("122", "down"), "300")
        self.assertEqual(get_neighbour("300", "down"), "302")
        self.assertEqual(get_neighbour("211", "down"), "213")

    def test_get_left(self):
        self.assertEqual(get_neighbour("033", "left"), "032")
        self.assertEqual(get_neighbour("122", "left"), "033")
        self.assertEqual(get_neighbour("300", "left"), "211")
        self.assertEqual(get_neighbour("211", "left"), "210")
