/* eslint-env browser */

/* eslint-disable no-undef */

function quadkeyToTile(qk) {
    let x = 0, y = 0, z = qk.length;
    for (let i = 0; i < z; i++) {
        const bit = z - i;
        const mask = 1 << (bit - 1);
        const q = qk[i];
        if (q === '1' || q === '3') x |= mask;
        if (q === '2' || q === '3') y |= mask;
    }
    return {z, x, y};
}

function tileToBBox(x, y, z) {
    const n = Math.pow(2, z);
    const lon1 = x / n * 360 - 180;
    const lon2 = (x + 1) / n * 360 - 180;
    const lat_rad_n = Math.atan(Math.sinh(Math.PI * (1 - 2 * y / n)));
    const lat_rad_s = Math.atan(Math.sinh(Math.PI * (1 - 2 * (y + 1) / n)));
    return [lon1, lat_rad_s * 180 / Math.PI, lon2, lat_rad_n * 180 / Math.PI];
}

function quadkeyToPolygon(qk, props) {
    const {z, x, y} = quadkeyToTile(qk);
    const [lon1, lat1, lon2, lat2] = tileToBBox(x, y, z);
    return {
        type: "Feature",
        geometry: {
            type: "Polygon",
            coordinates: [[[lon1, lat1], [lon2, lat1], [lon2, lat2], [lon1, lat2], [lon1, lat1]]]
        },
        properties: {...props, tile_quadkey: qk}
    };
}

let initialCenter = [-0.37, 43.3];
let initialZoom = 12;

try {
    const saved = localStorage.getItem('cam_viewer_view');
    if (saved) {
        const obj = JSON.parse(saved);
        if (Array.isArray(obj.center) && typeof obj.zoom === 'number') {
            initialCenter = obj.center;
            initialZoom = obj.zoom;
        }
    }
} catch (e) {
    console.warn('Failed to restore saved view', e);
}

const map = new maplibregl.Map({
    container: 'map',
    style: {
        version: 8,
        sources: {
            osm: {
                type: 'raster',
                tiles: ['https://tile.openstreetmap.org/{z}/{x}/{y}.png'],
                tileSize: 256,
                attribution: '© OpenStreetMap contributors'
            }
        },
        layers: [{id: 'osm', type: 'raster', source: 'osm'}]
    },
    center: initialCenter,
    zoom: initialZoom
});

// Add standard MapLibre controls
map.addControl(new maplibregl.NavigationControl(), 'top-right');
map.addControl(new maplibregl.ScaleControl({maxWidth: 200, unit: 'metric'}), 'bottom-left');
map.addControl(new maplibregl.FullscreenControl(), 'top-right');
map.addControl(new maplibregl.GeolocateControl({
    positionOptions: {enableHighAccuracy: true},
    trackUserLocation: true
}), 'top-right');

// Global state
let lastUpdateTime = null;
let tileCount = 0;

// Create info panel
function createInfoPanel() {
    const panel = document.createElement('div');
    panel.id = 'info-panel';
    panel.style.cssText = `
        position: absolute;
        bottom: 40px;
        left: 10px;
        background: rgba(255, 255, 255, 0.95);
        padding: 12px;
        border-radius: 5px;
        font-family: monospace;
        font-size: 11px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.3);
        z-index: 1000;
        min-width: 250px;
    `;
    panel.innerHTML = `
        <div style="font-weight: bold; margin-bottom: 8px; color: #333;">📊 Statistics</div>
        <div id="tile-count">Quadtrees: <span style="color: #2b83ba; font-weight: bold;">0</span></div>
        <div id="last-update">Last update: <span style="color: #666;">Never</span></div>
        <div id="refresh-status">Status: <span style="color: #7fbf7b;">●</span> Ready</div>
        <div id="bbox-info" style="margin-top: 8px; padding-top: 8px; border-top: 1px solid #ddd; font-size: 10px; color: #666;"></div>
    `;
    document.body.appendChild(panel);
}

function updateInfoPanel(status, count, bbox) {
    const now = new Date().toLocaleTimeString();
    document.getElementById('tile-count').innerHTML = `Quadtrees: <span style="color: #2b83ba; font-weight: bold;">${count}</span>`;
    document.getElementById('last-update').innerHTML = `Last update: <span style="color: #666;">${now}</span>`;

    const statusColors = {
        loading: '#fdae61',
        success: '#7fbf7b',
        error: '#d7191c'
    };
    const statusTexts = {
        loading: '● Loading...',
        success: '● Ready',
        error: '● Error'
    };
    document.getElementById('refresh-status').innerHTML =
        `Status: <span style="color: ${statusColors[status]};">${statusTexts[status]}</span>`;

    if (bbox) {
        document.getElementById('bbox-info').innerHTML =
            `BBox: [${bbox[0].toFixed(4)}, ${bbox[1].toFixed(4)}]<br>to [${bbox[2].toFixed(4)}, ${bbox[3].toFixed(4)}]`;
    }
}

map.on('load', async () => {
    createInfoPanel();

    async function loadTiles() {
        updateInfoPanel('loading', tileCount, null);

        try {
            const res = await fetch('/api/tiles');
            if (!res.ok) {
                const errorText = await res.text();
                throw new Error(`HTTP ${res.status}: ${errorText}`);
            }

            const tiles = await res.json();
            tileCount = tiles.length;

            console.log('📊 Total tiles:', tiles.length);

            const features = tiles.map(t => {
                if (!t.quadkey) {
                    console.warn('Tile missing quadkey:', t);
                    return null;
                }
                return quadkeyToPolygon(t.quadkey, {
                    mean_confidence: t.mean_confidence,
                    tile_count: t.count
                });
            }).filter(f => f !== null);

            // Calculate global bounding box
            let minLon = 180, minLat = 90, maxLon = -180, maxLat = -90;
            for (const f of features) {
                for (const [lon, lat] of f.geometry.coordinates[0]) {
                    if (lon < minLon) minLon = lon;
                    if (lon > maxLon) maxLon = lon;
                    if (lat < minLat) minLat = lat;
                    if (lat > maxLat) maxLat = lat;
                }
            }

            const bbox = [minLon, minLat, maxLon, maxLat];
            console.log(`📍 BBox: [${minLon.toFixed(4)}, ${minLat.toFixed(4)}] → [${maxLon.toFixed(4)}, ${maxLat.toFixed(4)}]`);

            // Update quadtrees source
            const src = map.getSource('quadtiles');
            if (src) {
                src.setData({type: 'FeatureCollection', features});
            } else {
                map.addSource('quadtiles', {
                    type: 'geojson',
                    data: {type: 'FeatureCollection', features}
                });
            }

            // Create red bounding box outline
            const bboxFeature = {
                type: 'Feature',
                geometry: {
                    type: 'Polygon',
                    coordinates: [[
                        [minLon, minLat],
                        [maxLon, minLat],
                        [maxLon, maxLat],
                        [minLon, maxLat],
                        [minLon, minLat]
                    ]]
                }
            };

            const bboxSrc = map.getSource('bbox-outline');
            if (bboxSrc) {
                bboxSrc.setData(bboxFeature);
            } else {
                map.addSource('bbox-outline', {
                    type: 'geojson',
                    data: bboxFeature
                });
            }

            // Add layers if first time
            if (!map.getLayer('quadtiles-fill')) {
                map.addLayer({
                    id: 'quadtiles-fill',
                    type: 'fill',
                    source: 'quadtiles',
                    paint: {
                        'fill-color': [
                            'case',
                            ['==', ['get', 'mean_confidence'], null],
                            '#cccccc',
                            [
                                'interpolate', ['linear'], ['get', 'mean_confidence'],
                                0, '#2b83ba',
                                25, '#7fbf7b',
                                50, '#ffffbf',
                                100, '#fdae61',
                                200, '#d7191c'
                            ]
                        ],
                        'fill-opacity': 0.6
                    }
                });

                map.addLayer({
                    id: 'quadtiles-outline',
                    type: 'line',
                    source: 'quadtiles',
                    paint: {
                        'line-color': '#000',
                        'line-width': 0.3,
                        'line-opacity': 0.4
                    }
                });

                map.addLayer({
                    id: 'bbox-outline-layer',
                    type: 'line',
                    source: 'bbox-outline',
                    paint: {
                        'line-color': '#d7191c',
                        'line-width': 3,
                        'line-dasharray': [4, 2]
                    }
                });
            }

            // Fit bounds on first load
            if (!lastUpdateTime && features.length > 0) {
                map.fitBounds([[minLon, minLat], [maxLon, maxLat]], {
                    padding: 50,
                    maxZoom: 14
                });
            }

            lastUpdateTime = Date.now();
            updateInfoPanel('success', tileCount, bbox);

        } catch (err) {
            console.error('❌ Failed to load tiles:', err);
            updateInfoPanel('error', tileCount, null);
            alert(`Error loading tiles: ${err.message}`);
        }
    }

    // Tooltip
    const popup = new maplibregl.Popup({
        closeButton: false,
        closeOnClick: false
    });

    map.on('mousemove', 'quadtiles-fill', (e) => {
        map.getCanvas().style.cursor = 'pointer';
        const p = e.features[0].properties;
        const mean = (p.mean_confidence === null || p.mean_confidence === undefined)
            ? '<span style="color: #999;">N/A</span>'
            : `<span style="color: #2b83ba; font-weight: bold;">${Number(p.mean_confidence).toFixed(1)} cm</span>`;
        const count = p.tile_count || 0;
        const qk = p.tile_quadkey || '';

        popup.setLngLat(e.lngLat)
            .setHTML(`
                <div style="font-family: monospace; font-size: 11px;">
                    <b>Quadkey:</b> ${qk}<br>
                    <b>Confidence:</b> ${mean}<br>
                    <b>Messages:</b> ${count}
                </div>
            `)
            .addTo(map);
    });

    map.on('mouseleave', 'quadtiles-fill', () => {
        map.getCanvas().style.cursor = '';
        popup.remove();
    });

    // Save view
    map.on('moveend', () => {
        try {
            const center = map.getCenter();
            localStorage.setItem('cam_viewer_view', JSON.stringify({
                center: [center.lng, center.lat],
                zoom: map.getZoom()
            }));
        } catch (e) {
            console.warn(e);
        }
    });

    // Manual refresh button
    const refreshBtn = document.createElement('button');
    refreshBtn.innerHTML = '🔄 Refresh';
    refreshBtn.style.cssText = `
        position: absolute;
        top: 10px;
        left: 10px;
        z-index: 999;
        padding: 8px 12px;
        font-size: 13px;
        cursor: pointer;
        background: white;
        border: 2px solid #2b83ba;
        border-radius: 4px;
        font-weight: bold;
        color: #2b83ba;
    `;
    refreshBtn.onclick = loadTiles;
    document.body.appendChild(refreshBtn);

    // Initial load
    await loadTiles();

    // ✅ NO AUTO-REFRESH - manual button only
});
