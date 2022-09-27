# Software Name: its-client
# SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
# SPDX-License-Identifier: MIT License
#
# This software is distributed under the MIT license, see LICENSE.txt file for more details.
#
# Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
# Software description: This Intelligent Transportation Systems (ITS)
# [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org)
# [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project
# for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
import unittest
from its_client import quadtree
from its_client.quadtree import (
    is_edgy,
    get_up_or_down,
    get_right_or_left,
    get_neighbour,
    get_neighborhood,
    slash,
    unslash,
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


class TestNeighborhood(unittest.TestCase):
    """
    |-------------------------------|-------------------------------|
    |000    |001    |010    |011    |100    |101    |110    |111    |
    |       |       |       |       |       |       |       |       |
    |-------------------------------|-------------------------------|
    |002    |003    |012    |013    |102    |103    |112    |113    |
    |       |       |       |       |       |       |       |       |
    |-------------------------------|-------------------------------|
    |020    |021    |030    |031    |120    |121    |130    |131    |
    |       |       |       |       |       |       |       |       |
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

    def test_top_left_corner_neighborhood(self):
        result = get_neighborhood("033")

        self.assertEqual(len(result), 8)
        self.assertTrue("030" in result)
        self.assertTrue("031" in result)
        self.assertTrue("120" in result)
        self.assertTrue("032" in result)
        self.assertTrue("122" in result)
        self.assertTrue("210" in result)
        self.assertTrue("211" in result)
        self.assertTrue("300" in result)

    def test_top_right_corner_neighborhood(self):
        result = get_neighborhood("122")

        self.assertEqual(len(result), 8)
        self.assertTrue("031" in result)
        self.assertTrue("120" in result)
        self.assertTrue("121" in result)
        self.assertTrue("033" in result)
        self.assertTrue("123" in result)
        self.assertTrue("211" in result)
        self.assertTrue("300" in result)
        self.assertTrue("301" in result)

    def test_bottom_left_corner_neighborhood(self):
        result = get_neighborhood("211")

        self.assertEqual(len(result), 8)
        self.assertTrue("032" in result)
        self.assertTrue("033" in result)
        self.assertTrue("122" in result)
        self.assertTrue("210" in result)
        self.assertTrue("300" in result)
        self.assertTrue("212" in result)
        self.assertTrue("213" in result)
        self.assertTrue("302" in result)

    def test_bottom_right_corner_neighborhood(self):
        result = get_neighborhood("300")

        self.assertEqual(len(result), 8)
        self.assertTrue("033" in result)
        self.assertTrue("122" in result)
        self.assertTrue("123" in result)
        self.assertTrue("211" in result)
        self.assertTrue("301" in result)
        self.assertTrue("213" in result)
        self.assertTrue("302" in result)
        self.assertTrue("303" in result)

    def test_slash_unslashed_key(self):
        unslashed_key = "01233210"

        result = slash(unslashed_key)

        self.assertEqual(result, "/0/1/2/3/3/2/1/0")

    def test_slash_slashed_key_returns_as_is(self):
        slashed_key = "/0/1/2/3/3/2/1/0"

        result = slash(slashed_key)

        self.assertEqual(result, "/0/1/2/3/3/2/1/0")

    def test_slash_non_key_str_returns_as_is(self):
        not_a_key = "This is not a quadkey"

        result = slash(not_a_key)

        self.assertEqual(result, "This is not a quadkey")

    def test_unslash_slashed_key(self):
        slashed_key = "/0/1/2/3/3/2/1/0"

        result = unslash(slashed_key)

        self.assertEqual(result, "01233210")

    def test_unslash_unslashed_key_returns_as_is(self):
        unslashed_key = "01233210"

        result = unslash(unslashed_key)

        self.assertEqual(result, "01233210")

    def test_unslash_non_key_str_returns_as_is(self):
        not_a_key = "This is not a quadkey"

        result = unslash(not_a_key)

        self.assertEqual(result, "This is not a quadkey")
