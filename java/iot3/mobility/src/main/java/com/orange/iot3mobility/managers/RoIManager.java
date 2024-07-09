/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3core.IoT3Core;
import com.orange.iot3mobility.quadkey.LatLng;
import com.orange.iot3mobility.quadkey.QuadTileHelper;

import java.util.ArrayList;

public class RoIManager {

    private final IoT3Core ioT3Core;

    private String topicRoot;
    private String uuid;

    private String subscriptionCamTopicBase = "/outQueue/v2x/cam/+";
    private String subscriptionCpmTopicBase = "/outQueue/v2x/cpm/+";
    private String subscriptionDenmTopicBase = "/outQueue/v2x/denm/+";
    private String subscriptionDenmTopicPrivate = "/outQueue/v2x/denm/";

    private String currentCamKey;
    private String currentCpmKey;
    private String currentDenmKey;

    private final ArrayList<String> CURRENT_CAM_KEYS = new ArrayList<>();
    private final ArrayList<String> CURRENT_CPM_KEYS = new ArrayList<>();
    private final ArrayList<String> CURRENT_DENM_KEYS = new ArrayList<>();

    public RoIManager(IoT3Core ioT3Core, String uuid, String topicRoot) {
        this.ioT3Core = ioT3Core;
        this.topicRoot = topicRoot;
        this.uuid = uuid;

        subscriptionCamTopicBase = topicRoot + subscriptionCamTopicBase;
        subscriptionDenmTopicBase = topicRoot + subscriptionDenmTopicBase;
        subscriptionCpmTopicBase = topicRoot + subscriptionCpmTopicBase;
        subscriptionDenmTopicPrivate = topicRoot + subscriptionDenmTopicPrivate + uuid;

        ioT3Core.mqttSubscribe(subscriptionDenmTopicPrivate);
    }

    /**
     * Sets the Region of Interest (RoI) for road users based on a specific
     * geographical position and zoom level.
     *
     * @param position the LatLng representing the target geographical position for the RoI.
     * @param level the zoom level for the RoI, which should be between 1 and 22. This level
     *              determines the granularity of the quadtree tile(s) computed around the target
     *              position.
     * @param withNeighborTiles include neighboring tiles around the computed target tile,
     *                          i.e. the RoI will be 1 tile or 9 tiles (recommended for clients
     *                          changing position such as vehicles or vulnerable users)
     */
    public void setRoadUserRoI(LatLng position, int level, boolean withNeighborTiles) {
        String quadkey = QuadTileHelper.latLngToQuadKey(position.latitude, position.longitude,
                level);
        if(!quadkey.equals(currentCamKey)) {
            // get the list of tiles
            ArrayList<String> tileKeys = getTiles(quadkey, withNeighborTiles);
            updateSubscriptions(tileKeys, CURRENT_CAM_KEYS, subscriptionCamTopicBase, level < 22);
            // save the current state
            CURRENT_CAM_KEYS.clear();
            CURRENT_CAM_KEYS.addAll(tileKeys);
            currentCamKey = quadkey;
        }
    }

    /**
     * Sets the Region of Interest (RoI) for road hazards based on a specific
     * geographical position and zoom level.
     *
     * @param position the LatLng representing the target geographical position for the RoI.
     * @param level the zoom level for the RoI, which should be between 1 and 22. This level
     *              determines the granularity of the quadtree tile(s) computed around the target
     *              position.
     * @param withNeighborTiles include neighboring tiles around the computed target tile,
     *                          i.e. the RoI will be 1 tile or 9 tiles (recommended for clients
     *                          changing position such as vehicles or vulnerable users)
     */
    public void setRoadHazardRoI(LatLng position, int level, boolean withNeighborTiles) {
        String quadkey = QuadTileHelper.latLngToQuadKey(position.latitude, position.longitude,
                level);
        if(!quadkey.equals(currentDenmKey)) {
            // get the list of tiles
            ArrayList<String> tileKeys = getTiles(quadkey, withNeighborTiles);
            updateSubscriptions(tileKeys, CURRENT_DENM_KEYS, subscriptionDenmTopicBase, level < 22);
            // save the current state
            CURRENT_DENM_KEYS.clear();
            CURRENT_DENM_KEYS.addAll(tileKeys);
            currentDenmKey = quadkey;
        }
    }

    /**
     * Sets the Region of Interest (RoI) for road sensors based on a specific
     * geographical position and zoom level.
     *
     * @param position the LatLng representing the target geographical position for the RoI.
     * @param level the zoom level for the RoI, which should be between 1 and 22. This level
     *              determines the granularity of the quadtree tile(s) computed around the target
     *              position.
     * @param withNeighborTiles include neighboring tiles around the computed target tile,
     *                          i.e. the RoI will be 1 tile or 9 tiles (recommended for clients
     *                          changing position such as vehicles or vulnerable users)
     */
    public void setRoadSensorRoI(LatLng position, int level, boolean withNeighborTiles) {
        String quadkey = QuadTileHelper.latLngToQuadKey(position.latitude, position.longitude,
                level);
        if(!quadkey.equals(currentCpmKey)) {
            // get the list of tiles
            ArrayList<String> tileKeys = getTiles(quadkey, withNeighborTiles);
            updateSubscriptions(tileKeys, CURRENT_CPM_KEYS, subscriptionCpmTopicBase, level < 22);
            // save the current state
            CURRENT_CPM_KEYS.clear();
            CURRENT_CPM_KEYS.addAll(tileKeys);
            currentCpmKey = quadkey;
        }
    }

    private ArrayList<String> getTiles(String quadkey, boolean withNeighborTiles) {
        // build the list of tiles
        ArrayList<String> tileKeys = new ArrayList<>();
        // center tile
        tileKeys.add(quadkey);
        // + 8 neighboring tiles if requested
        if(withNeighborTiles) tileKeys.addAll(QuadTileHelper.getNeighborQuadKeys(quadkey));
        return tileKeys;
    }

    private void updateSubscriptions(ArrayList<String> newKeys,
                                            ArrayList<String> currentKeys,
                                            String topicBase, boolean addWildcard) {
        // subscribe to all the new tiles
        for(String newKey: newKeys) {
            if(!currentKeys.contains(newKey)) {
                String topic = topicBase + QuadTileHelper.quadKeyToQuadTopic(newKey);
                if(addWildcard) topic += "/#";
                ioT3Core.mqttSubscribe(topic);
            }
        }
        // unsubscribe from all the tiles which are no longer needed
        for(String currentKey: currentKeys) {
            if(!newKeys.contains(currentKey)) {
                String topic = topicBase + QuadTileHelper.quadKeyToQuadTopic(currentKey);
                if(addWildcard) topic += "/#";
                ioT3Core.mqttUnsubscribe(topic);
            }
        }
    }
}
