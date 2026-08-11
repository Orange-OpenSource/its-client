/* eslint-env browser */
/* eslint-disable no-undef */

/*
 * Software Name : libits-client
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 *
 * This software is distributed under the MIT license,
 * see the "LICENSE.txt" file for more details or https://opensource.org/license/MIT/
 *
 * Authors: see CONTRIBUTORS.md
 */

/**
 * @typedef {Object} TileApiRecord
 * @property {string} quadkey
 * @property {string} message_type
 * @property {string} day
 * @property {number} count
 * @property {number} mean_position_confidence
 */

// ── Quadkey utilities ─────────────────────────────────────────────────────

function quadkeyToTile(quadkey) {
    let tileX = 0, tileY = 0;
    const zoom = quadkey.length;
    for (let index = 0; index < zoom; index++) {
        const bit = zoom - index;
        const mask = 1 << (bit - 1);
        const digit = quadkey[index];
        if (digit === '1' || digit === '3') tileX |= mask;
        if (digit === '2' || digit === '3') tileY |= mask;
    }
    return {zoom, tileX, tileY};
}

function tileToBBox(tileX, tileY, zoom) {
    const tileCount = Math.pow(2, zoom);
    const westLon = tileX / tileCount * 360 - 180;
    const eastLon = (tileX + 1) / tileCount * 360 - 180;
    const northLatRad = Math.atan(Math.sinh(Math.PI * (1 - 2 * tileY / tileCount)));
    const southLatRad = Math.atan(Math.sinh(Math.PI * (1 - 2 * (tileY + 1) / tileCount)));
    return [westLon, southLatRad * 180 / Math.PI, eastLon, northLatRad * 180 / Math.PI];
}

function quadkeyToPolygon(quadkey, properties) {
    const {zoom, tileX, tileY} = quadkeyToTile(quadkey);
    const [westLon, southLat, eastLon, northLat] = tileToBBox(tileX, tileY, zoom);
    return {
        type: "Feature",
        geometry: {
            type: "Polygon",
            coordinates: [[[westLon, southLat], [eastLon, southLat], [eastLon, northLat], [westLon, northLat], [westLon, southLat]]]
        },
        properties: {...properties, tile_quadkey: quadkey}
    };
}

// ── Color scales ──────────────────────────────────────────────────────────

// Viridis-inspired scale for count metric
const COUNT_COLORS = [
    {value: 0, color: '#440154'},
    {value: 10, color: '#482878'},
    {value: 50, color: '#3e4989'},
    {value: 100, color: '#31688e'},
    {value: 500, color: '#26828e'},
    {value: 1000, color: '#1f9e89'},
    {value: 5000, color: '#6cce5a'},
    {value: 10000, color: '#b6de2b'},
    {value: 50000, color: '#fee825'}
];

// Blue-to-red diverging scale for confidence
const CONFIDENCE_COLORS = [
    {value: 0, color: '#2166ac'},
    {value: 25, color: '#67a9cf'},
    {value: 50, color: '#d1e5f0'},
    {value: 100, color: '#fddbc7'},
    {value: 200, color: '#ef8a62'},
    {value: 500, color: '#b2182b'}
];

function getColorScale(metricName) {
    return metricName === 'count' ? COUNT_COLORS : CONFIDENCE_COLORS;
}

function buildFillColorExpression(metricName) {
    const scale = getColorScale(metricName);
    const propertyName = metricName === 'count' ? 'tile_count' : 'tile_confidence';
    const interpolation = ['interpolate', ['linear'], ['get', propertyName]];
    for (const stop of scale) {
        interpolation.push(stop.value, stop.color);
    }
    return interpolation;
}

function updateLegend(metricName) {
    const scale = getColorScale(metricName);
    const label = metricName === 'count' ? 'Message Count' : 'Mean Position Confidence (cm)';
    let html = `<div class="legend-title">${label}</div>`;
    for (let index = 0; index < scale.length - 1; index++) {
        html += `<div class="legend-item">
            <div class="legend-color" style="background:${scale[index].color};"></div>
            <span>${scale[index].value} – ${scale[index + 1].value}</span>
        </div>`;
    }
    html += `<div class="legend-item">
        <div class="legend-color" style="background:${scale[scale.length - 1].color};"></div>
        <span>${scale[scale.length - 1].value}+</span>
    </div>`;
    document.getElementById('legend').innerHTML = html;
}

// ── Map initialization ────────────────────────────────────────────────────

let initialCenter = [2.35, 48.85];
let initialZoom = 6;

try {
    const savedView = localStorage.getItem('its_viewer_view');
    if (savedView) {
        const parsed = JSON.parse(savedView);
        if (Array.isArray(parsed.center) && typeof parsed.zoom === 'number') {
            initialCenter = parsed.center;
            initialZoom = parsed.zoom;
        }
    }
} catch (error) {
    console.warn('Failed to restore saved view', error);
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

map.addControl(new maplibregl.NavigationControl(), 'top-right');
map.addControl(new maplibregl.ScaleControl({maxWidth: 200, unit: 'metric'}), 'bottom-left');

// ── Rectangle selection state ─────────────────────────────────────────────

let isSelectingRectangle = false;
let selectionStartPoint = null;
let selectionBoxElement = null;

function toggleRectangleSelection() {
    isSelectingRectangle = !isSelectingRectangle;
    const selectButton = document.getElementById('select-btn');
    const selectionHint = document.getElementById('selection-hint');

    if (isSelectingRectangle) {
        selectButton.textContent = '📐 Cancel Selection';
        selectButton.classList.remove('btn-warning');
        selectButton.classList.add('btn-secondary');
        selectionHint.style.display = 'block';
        map.getCanvas().style.cursor = 'crosshair';
        // Disable map drag
        map.dragPan.disable();
    } else {
        selectButton.textContent = '📐 Select Zone';
        selectButton.classList.remove('btn-secondary');
        selectButton.classList.add('btn-warning');
        selectionHint.style.display = 'none';
        map.getCanvas().style.cursor = '';
        map.dragPan.enable();
        removeSelectionBox();
    }
}

function removeSelectionBox() {
    if (selectionBoxElement) {
        selectionBoxElement.remove();
        selectionBoxElement = null;
    }
}

map.getCanvas().addEventListener('mousedown', (event) => {
    if (!isSelectingRectangle) return;
    selectionStartPoint = {x: event.clientX, y: event.clientY};
    document.getElementById('selection-hint').style.display = 'none';

    selectionBoxElement = document.createElement('div');
    selectionBoxElement.style.cssText = `
        position: absolute;
        border: 2px dashed #2b83ba;
        background: rgba(43, 131, 186, 0.15);
        pointer-events: none;
        z-index: 1500;
    `;
    document.body.appendChild(selectionBoxElement);
});

document.addEventListener('mousemove', (event) => {
    if (!isSelectingRectangle || !selectionStartPoint || !selectionBoxElement) return;

    const minX = Math.min(selectionStartPoint.x, event.clientX);
    const minY = Math.min(selectionStartPoint.y, event.clientY);
    const width = Math.abs(event.clientX - selectionStartPoint.x);
    const height = Math.abs(event.clientY - selectionStartPoint.y);

    selectionBoxElement.style.left = minX + 'px';
    selectionBoxElement.style.top = minY + 'px';
    selectionBoxElement.style.width = width + 'px';
    selectionBoxElement.style.height = height + 'px';
});

document.addEventListener('mouseup', (event) => {
    if (!isSelectingRectangle || !selectionStartPoint) return;

    const endPoint = {x: event.clientX, y: event.clientY};

    // Convert screen coordinates to geographic coordinates
    const startLngLat = map.unproject([selectionStartPoint.x, selectionStartPoint.y]);
    const endLngLat = map.unproject([endPoint.x, endPoint.y]);

    const minLon = Math.min(startLngLat.lng, endLngLat.lng);
    const maxLon = Math.max(startLngLat.lng, endLngLat.lng);
    const minLat = Math.min(startLngLat.lat, endLngLat.lat);
    const maxLat = Math.max(startLngLat.lat, endLngLat.lat);

    // Update bbox inputs
    document.getElementById('bbox-min-lon').value = minLon.toFixed(4);
    document.getElementById('bbox-min-lat').value = minLat.toFixed(4);
    document.getElementById('bbox-max-lon').value = maxLon.toFixed(4);
    document.getElementById('bbox-max-lat').value = maxLat.toFixed(4);

    // Zoom to selection
    map.fitBounds([[minLon, minLat], [maxLon, maxLat]], {padding: 30});

    // Exit selection mode
    selectionStartPoint = null;
    removeSelectionBox();
    toggleRectangleSelection();

    // Auto-load with new bbox
    loadTiles().then(_ => {
    }).catch(e => console.error(e));
});

function clearBbox() {
    document.getElementById('bbox-min-lon').value = '';
    document.getElementById('bbox-min-lat').value = '';
    document.getElementById('bbox-max-lon').value = '';
    document.getElementById('bbox-max-lat').value = '';

    // Remove the selection rectangle source/layer if present
    if (map.getLayer('selection-rect-layer')) {
        map.removeLayer('selection-rect-layer');
    }
    if (map.getSource('selection-rect')) {
        map.removeSource('selection-rect');
    }
}

// ── Data loading ──────────────────────────────────────────────────────────

let isFirstLoad = true;
let autoLoadTimer = null;
let availableDays = [];
let availableDaySet = new Set();
let dayInputElement = null;
let dayPickerButton = null;
let calendarPopup = null;
let calendarTitleElement = null;
let calendarGridElement = null;
let calendarMonthDate = new Date();

function parseDayString(dateStr) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) return null;

    const [year, month, day] = dateStr.split('-').map(Number);
    const parsedDate = new Date(year, month - 1, day, 12);
    if (parsedDate.getFullYear() !== year || parsedDate.getMonth() !== month - 1 || parsedDate.getDate() !== day) {
        return null;
    }
    return parsedDate;
}

function formatDay(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function isCalendarOpen() {
    return Boolean(calendarPopup && calendarPopup.classList.contains('open'));
}

function setCalendarMonth(date) {
    calendarMonthDate = new Date(date.getFullYear(), date.getMonth(), 1, 12);
}

function getCalendarAnchorDate() {
    return parseDayString(dayInputElement?.value || '')
        || parseDayString(availableDays[availableDays.length - 1] || '')
        || new Date();
}

function closeCalendar() {
    if (!calendarPopup || !dayPickerButton) return;
    calendarPopup.classList.remove('open');
    dayPickerButton.classList.remove('active');
    dayPickerButton.setAttribute('aria-expanded', 'false');
}

function renderCalendar(year = calendarMonthDate.getFullYear(), month = calendarMonthDate.getMonth()) {
    if (!calendarTitleElement || !calendarGridElement) return;

    setCalendarMonth(new Date(year, month, 1, 12));
    const selectedDate = parseDayString(dayInputElement?.value || '');
    const selectedDay = selectedDate ? formatDay(selectedDate) : '';
    const today = formatDay(new Date());

    calendarTitleElement.textContent = new Date(year, month, 1).toLocaleString('en-US', {
        month: 'long',
        year: 'numeric'
    });
    calendarGridElement.innerHTML = '';

    const firstDayOfMonth = new Date(year, month, 1, 12);
    const offset = (firstDayOfMonth.getDay() + 6) % 7;
    const gridStart = new Date(year, month, 1 - offset, 12);

    for (let index = 0; index < 42; index++) {
        const cellDate = new Date(gridStart.getFullYear(), gridStart.getMonth(), gridStart.getDate() + index, 12);
        const dateStr = formatDay(cellDate);
        const dayButton = document.createElement('button');
        dayButton.type = 'button';
        dayButton.className = 'calendar-day';
        dayButton.textContent = String(cellDate.getDate());
        dayButton.setAttribute('aria-label', dateStr);

        if (cellDate.getMonth() !== month) dayButton.classList.add('is-outside');
        if (dateStr === today) dayButton.classList.add('is-today');
        if (dateStr === selectedDay) dayButton.classList.add('is-selected');
        if (availableDaySet.has(dateStr)) dayButton.classList.add('has-data');

        dayButton.addEventListener('click', () => selectDay(dateStr));
        calendarGridElement.appendChild(dayButton);
    }
}

function openCalendar() {
    if (!calendarPopup || !dayPickerButton) return;
    const anchorDate = getCalendarAnchorDate();
    renderCalendar(anchorDate.getFullYear(), anchorDate.getMonth());
    calendarPopup.classList.add('open');
    dayPickerButton.classList.add('active');
    dayPickerButton.setAttribute('aria-expanded', 'true');
}

function toggleCalendar() {
    if (isCalendarOpen()) {
        closeCalendar();
    } else {
        openCalendar();
    }
}

function selectDay(dateStr, {close = true, triggerLoad = true} = {}) {
    const parsedDate = parseDayString(dateStr);
    if (!parsedDate || !dayInputElement) return;

    dayInputElement.value = formatDay(parsedDate);
    renderCalendar(parsedDate.getFullYear(), parsedDate.getMonth());

    if (close) closeCalendar();
    if (triggerLoad) loadTiles().then(_ => {
    }).catch(e => console.error(e));
}

function shiftSelectedDay(dayOffset) {
    if (!dayInputElement) return;

    const parsedDate = parseDayString(dayInputElement.value);
    if (!parsedDate) return;

    parsedDate.setDate(parsedDate.getDate() + dayOffset);
    selectDay(formatDay(parsedDate), {close: false});
}

function initializeDayPicker() {
    dayInputElement = document.getElementById('day-input');
    const dayPickerWrapper = document.getElementById('day-picker');
    dayPickerButton = document.getElementById('day-picker-toggle');
    if (!dayInputElement || !dayPickerWrapper || !dayPickerButton) return;

    calendarPopup = document.createElement('div');
    calendarPopup.className = 'calendar-popup';
    calendarPopup.innerHTML = `
        <div class="calendar-header">
            <button type="button" class="calendar-nav-btn" data-direction="-1" aria-label="Previous month">‹</button>
            <div class="calendar-title"></div>
            <button type="button" class="calendar-nav-btn" data-direction="1" aria-label="Next month">›</button>
        </div>
        <div class="calendar-weekdays">
            <div class="calendar-weekday">Mon</div>
            <div class="calendar-weekday">Tue</div>
            <div class="calendar-weekday">Wed</div>
            <div class="calendar-weekday">Thu</div>
            <div class="calendar-weekday">Fri</div>
            <div class="calendar-weekday">Sat</div>
            <div class="calendar-weekday">Sun</div>
        </div>
        <div class="calendar-grid"></div>
    `;
    dayPickerWrapper.appendChild(calendarPopup);

    calendarTitleElement = calendarPopup.querySelector('.calendar-title');
    calendarGridElement = calendarPopup.querySelector('.calendar-grid');

    for (const button of calendarPopup.querySelectorAll('.calendar-nav-btn')) {
        button.addEventListener('click', () => {
            const direction = Number(button.dataset.direction);
            const nextMonth = new Date(calendarMonthDate.getFullYear(), calendarMonthDate.getMonth() + direction, 1, 12);
            renderCalendar(nextMonth.getFullYear(), nextMonth.getMonth());
        });
    }

    dayPickerButton.addEventListener('click', (event) => {
        event.preventDefault();
        toggleCalendar();
    });

    dayInputElement.addEventListener('keydown', (event) => {
        if (event.key === 'ArrowUp') {
            if (parseDayString(dayInputElement.value)) {
                event.preventDefault();
                shiftSelectedDay(1);
            }
        } else if (event.key === 'ArrowDown') {
            if (parseDayString(dayInputElement.value)) {
                event.preventDefault();
                shiftSelectedDay(-1);
            }
        } else if (event.key === 'Escape') {
            closeCalendar();
        }
    });

    dayInputElement.addEventListener('input', () => {
        const parsedDate = parseDayString(dayInputElement.value);
        if (parsedDate && isCalendarOpen()) {
            renderCalendar(parsedDate.getFullYear(), parsedDate.getMonth());
        }
    });

    document.addEventListener('mousedown', (event) => {
        if (isCalendarOpen() && !dayPickerWrapper.contains(event.target)) {
            closeCalendar();
        }
    });

    renderCalendar(calendarMonthDate.getFullYear(), calendarMonthDate.getMonth());
}

async function loadMetadata() {
    try {
        const response = await fetch('/api/metadata');
        const metadata = await response.json();

        availableDays = Array.isArray(metadata.days) ? metadata.days : [];
        availableDaySet = new Set(availableDays);

        const dayInput = document.getElementById('day-input');
        const dayOptions = document.getElementById('day-options');
        dayOptions.innerHTML = '';
        for (const day of availableDays) {
            const option = document.createElement('option');
            option.value = day;
            dayOptions.appendChild(option);
        }
        // Pre-fill with last day if not set
        if (!dayInput.value && availableDays.length > 0) {
            dayInput.value = availableDays[availableDays.length - 1];
        }

        const anchorDate = parseDayString(dayInput.value)
            || parseDayString(availableDays[availableDays.length - 1] || '')
            || new Date();
        renderCalendar(anchorDate.getFullYear(), anchorDate.getMonth());

        const typeSelect = document.getElementById('type-select');
        typeSelect.innerHTML = '<option value="">All types</option>';
        // Ensure consistent ordering and presence of CPM perceived objects
        const sortedTypes = [...metadata.message_types].sort((a, b) => a.localeCompare(b));
        for (const messageType of sortedTypes) {
            const option = document.createElement('option');
            option.value = messageType;
            option.textContent = messageType.toUpperCase();
            typeSelect.appendChild(option);
        }
    } catch (error) {
        console.error('Failed to load metadata:', error);
    }
}

async function loadTiles() {
    const selectedDay = document.getElementById('day-input').value;
    const selectedType = document.getElementById('type-select').value;
    const selectedMetric = document.getElementById('metric-select').value;

    const minLon = document.getElementById('bbox-min-lon').value;
    const minLat = document.getElementById('bbox-min-lat').value;
    const maxLon = document.getElementById('bbox-max-lon').value;
    const maxLat = document.getElementById('bbox-max-lat').value;

    let queryUrl = '/api/tiles?';
    if (selectedDay) queryUrl += `day=${encodeURIComponent(selectedDay)}&`;
    if (selectedType) queryUrl += `message_type=${encodeURIComponent(selectedType)}&`;
    if (minLon && minLat && maxLon && maxLat) {
        queryUrl += `min_lon=${minLon}&min_lat=${minLat}&max_lon=${maxLon}&max_lat=${maxLat}&`;
    }

    updateStatus('loading');

    try {
        const response = await fetch(queryUrl);
        if (!response.ok) {
            console.error(`HTTP ${response.status}`);
            updateStatus('error');
            return;
        }

        /** @type {TileApiRecord[]} */
        const tiles = await response.json();

        const features = tiles.map(tile => {
            if (!tile.quadkey) return null;
            return quadkeyToPolygon(tile.quadkey, {
                tile_count: tile.count,
                tile_confidence: tile.mean_position_confidence,
                message_type: tile.message_type
            });
        }).filter(feature => feature !== null);

        // Update map source
        const existingSource = map.getSource('quadtiles');
        if (existingSource) {
            existingSource.setData({type: 'FeatureCollection', features});
        } else {
            map.addSource('quadtiles', {
                type: 'geojson',
                data: {type: 'FeatureCollection', features}
            });
        }

        // Add or update layers
        if (map.getLayer('quadtiles-fill')) {
            map.removeLayer('quadtiles-fill');
            map.removeLayer('quadtiles-outline');
        }

        map.addLayer({
            id: 'quadtiles-fill',
            type: 'fill',
            source: 'quadtiles',
            paint: {
                'fill-color': buildFillColorExpression(selectedMetric),
                'fill-opacity': 0.65
            }
        });

        map.addLayer({
            id: 'quadtiles-outline',
            type: 'line',
            source: 'quadtiles',
            paint: {
                'line-color': '#333',
                'line-width': 0.3,
                'line-opacity': 0.4
            }
        });

        // Draw selection rectangle on map
        if (minLon && minLat && maxLon && maxLat) {
            drawSelectionRect(
                parseFloat(minLon), parseFloat(minLat),
                parseFloat(maxLon), parseFloat(maxLat)
            );
        }

        // Update legend
        updateLegend(selectedMetric);

        // Stats
        const totalMessages = tiles.reduce((sum, tile) => sum + tile.count, 0);
        document.getElementById('stat-tiles').innerHTML = `Tiles: <b>${features.length}</b>`;
        document.getElementById('stat-messages').innerHTML = `Messages: <b>${totalMessages.toLocaleString()}</b>`;
        updateStatus('success');

        // Fit bounds on first load only if no bbox filter
        if (isFirstLoad && !minLon && features.length > 0) {
            let boundsMinLon = 180, boundsMinLat = 90, boundsMaxLon = -180, boundsMaxLat = -90;
            for (const feature of features) {
                for (const [lon, lat] of feature.geometry.coordinates[0]) {
                    if (lon < boundsMinLon) boundsMinLon = lon;
                    if (lon > boundsMaxLon) boundsMaxLon = lon;
                    if (lat < boundsMinLat) boundsMinLat = lat;
                    if (lat > boundsMaxLat) boundsMaxLat = lat;
                }
            }
            map.fitBounds([[boundsMinLon, boundsMinLat], [boundsMaxLon, boundsMaxLat]], {
                padding: 50,
                maxZoom: 16
            });
            isFirstLoad = false;
        }

    } catch (error) {
        console.error('Failed to load tiles:', error);
        updateStatus('error');
    }
}

function startAutoLoad() {
    stopAutoLoad();
    loadTiles().then(_ => {
    }).catch(e => console.error(e));
    autoLoadTimer = setInterval(() => {
        loadTiles().then(_ => {
        }).catch(e => console.error(e));
    }, 5000);
}

function stopAutoLoad() {
    if (autoLoadTimer) {
        clearInterval(autoLoadTimer);
        autoLoadTimer = null;
    }
}

function drawSelectionRect(minLon, minLat, maxLon, maxLat) {
    const rectFeature = {
        type: 'Feature',
        geometry: {
            type: 'Polygon',
            coordinates: [[
                [minLon, minLat], [maxLon, minLat],
                [maxLon, maxLat], [minLon, maxLat],
                [minLon, minLat]
            ]]
        }
    };

    const existingSource = map.getSource('selection-rect');
    if (existingSource) {
        existingSource.setData(rectFeature);
    } else {
        map.addSource('selection-rect', {type: 'geojson', data: rectFeature});
    }

    if (!map.getLayer('selection-rect-layer')) {
        map.addLayer({
            id: 'selection-rect-layer',
            type: 'line',
            source: 'selection-rect',
            paint: {
                'line-color': '#d7191c',
                'line-width': 2.5,
                'line-dasharray': [4, 2]
            }
        });
    }
}

function updateStatus(status) {
    const statusColors = {loading: '#fdae61', success: '#7fbf7b', error: '#d7191c'};
    const statusTexts = {loading: '● Loading...', success: '● Ready', error: '● Error'};
    document.getElementById('stat-status').innerHTML =
        `Status: <span style="color:${statusColors[status]};">${statusTexts[status]}</span>`;
}

// ── Tooltip ───────────────────────────────────────────────────────────────

const popup = new maplibregl.Popup({closeButton: false, closeOnClick: false});

map.on('mousemove', 'quadtiles-fill', (event) => {
    map.getCanvas().style.cursor = 'pointer';
    const properties = event.features[0].properties;
    const count = properties.tile_count || 0;
    const confidence = (properties.tile_confidence !== null && properties.tile_confidence !== undefined)
        ? `${Number(properties.tile_confidence).toFixed(1)} cm`
        : 'N/A';
    const quadkey = properties.tile_quadkey || '';
    const messageType = properties.message_type || '';

    popup.setLngLat(event.lngLat)
        .setHTML(`
            <div style="font-family: monospace; font-size: 11px; line-height: 1.6;">
                <b>Quadkey:</b> ${quadkey}<br>
                <b>Type:</b> ${messageType}<br>
                <b>Count:</b> ${count.toLocaleString()}<br>
                <b>Confidence:</b> ${confidence}
            </div>
        `)
        .addTo(map);
});

map.on('mouseleave', 'quadtiles-fill', () => {
    map.getCanvas().style.cursor = '';
    popup.remove();
});

// ── Save view ─────────────────────────────────────────────────────────────

map.on('moveend', () => {
    try {
        const center = map.getCenter();
        localStorage.setItem('its_viewer_view', JSON.stringify({
            center: [center.lng, center.lat],
            zoom: map.getZoom()
        }));
    } catch (error) {
        console.warn(error);
    }
});

// ── Init ──────────────────────────────────────────────────────────────────

map.on('load', async () => {
    initializeDayPicker();
    await loadMetadata();
    updateLegend('count');
    const autoLoadCheckbox = document.getElementById('auto-load');
    const shouldAutoLoad = !!autoLoadCheckbox?.checked;

    // If autoload is enabled, the interval will trigger an immediate load.
    // Avoid double-loading on startup.
    if (shouldAutoLoad) {
        startAutoLoad();
    } else {
        await loadTiles();
    }
    // Auto-reload when any selector changes
    document.getElementById('day-input').addEventListener('change', () => {
        const parsedDate = parseDayString(document.getElementById('day-input').value);
        if (parsedDate) {
            renderCalendar(parsedDate.getFullYear(), parsedDate.getMonth());
        }
        loadTiles();
    });
    document.getElementById('type-select').addEventListener('change', () => loadTiles());
    document.getElementById('metric-select').addEventListener('change', () => loadTiles());

    autoLoadCheckbox.addEventListener('change', (event) => {
        if (event.target.checked) {
            startAutoLoad();
        } else {
            stopAutoLoad();
        }
    });
});
