/*
 * Software Name : libits-client
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 */

use anyhow::Result;
use rusqlite::Connection;
use std::collections::HashMap;

struct City {
    name: &'static str,
    lat: f64,
    lon: f64,
}

const FRENCH_CITIES: &[City] = &[
    City {
        name: "Paris",
        lat: 48.8566,
        lon: 2.3522,
    },
    City {
        name: "Marseille",
        lat: 43.2965,
        lon: 5.3698,
    },
    City {
        name: "Lyon",
        lat: 45.7640,
        lon: 4.8357,
    },
    City {
        name: "Toulouse",
        lat: 43.6047,
        lon: 1.4442,
    },
    City {
        name: "Nice",
        lat: 43.7102,
        lon: 7.2620,
    },
    City {
        name: "Nantes",
        lat: 47.2184,
        lon: -1.5536,
    },
    City {
        name: "Strasbourg",
        lat: 48.5734,
        lon: 7.7521,
    },
    City {
        name: "Montpellier",
        lat: 43.6108,
        lon: 3.8767,
    },
    City {
        name: "Bordeaux",
        lat: 44.8378,
        lon: -0.5792,
    },
    City {
        name: "Lille",
        lat: 50.6292,
        lon: 3.0573,
    },
    City {
        name: "Rennes",
        lat: 48.1173,
        lon: -1.6778,
    },
    City {
        name: "Reims",
        lat: 49.2583,
        lon: 4.0317,
    },
    City {
        name: "Le Havre",
        lat: 49.4944,
        lon: 0.1079,
    },
    City {
        name: "Saint-Etienne",
        lat: 45.4397,
        lon: 4.3872,
    },
    City {
        name: "Toulon",
        lat: 43.1242,
        lon: 5.9280,
    },
    City {
        name: "Grenoble",
        lat: 45.1885,
        lon: 5.7245,
    },
    City {
        name: "Dijon",
        lat: 47.3220,
        lon: 5.0415,
    },
    City {
        name: "Angers",
        lat: 47.4784,
        lon: -0.5632,
    },
    City {
        name: "Nimes",
        lat: 43.8367,
        lon: 4.3601,
    },
    City {
        name: "Clermont-Ferrand",
        lat: 45.7772,
        lon: 3.0870,
    },
    City {
        name: "Le Mans",
        lat: 48.0077,
        lon: 0.1984,
    },
    City {
        name: "Aix-en-Provence",
        lat: 43.5297,
        lon: 5.4474,
    },
    City {
        name: "Brest",
        lat: 48.3905,
        lon: -4.4861,
    },
    City {
        name: "Tours",
        lat: 47.3941,
        lon: 0.6848,
    },
    City {
        name: "Amiens",
        lat: 49.8941,
        lon: 2.2958,
    },
    City {
        name: "Limoges",
        lat: 45.8336,
        lon: 1.2611,
    },
    City {
        name: "Annecy",
        lat: 45.8992,
        lon: 6.1294,
    },
    City {
        name: "Perpignan",
        lat: 42.6886,
        lon: 2.8948,
    },
    City {
        name: "Metz",
        lat: 49.1193,
        lon: 6.1757,
    },
    City {
        name: "Besancon",
        lat: 47.2380,
        lon: 6.0243,
    },
    City {
        name: "Orleans",
        lat: 47.9029,
        lon: 1.9093,
    },
    City {
        name: "Rouen",
        lat: 49.4432,
        lon: 1.0993,
    },
    City {
        name: "Mulhouse",
        lat: 47.7508,
        lon: 7.3359,
    },
    City {
        name: "Caen",
        lat: 49.1829,
        lon: -0.3707,
    },
    City {
        name: "Nancy",
        lat: 48.6921,
        lon: 6.1844,
    },
    City {
        name: "Pau",
        lat: 43.2951,
        lon: -0.3708,
    },
    City {
        name: "Bayonne",
        lat: 43.4832,
        lon: -1.4748,
    },
    City {
        name: "Tarbes",
        lat: 43.2333,
        lon: 0.0833,
    },
    City {
        name: "Lourdes",
        lat: 43.0947,
        lon: -0.0464,
    },
    City {
        name: "Biarritz",
        lat: 43.4832,
        lon: -1.5586,
    },
    City {
        name: "Anglet",
        lat: 43.4833,
        lon: -1.5167,
    },
];

fn haversine_distance(lat1: f64, lon1: f64, lat2: f64, lon2: f64) -> f64 {
    const R: f64 = 6371.0;
    let d_lat = (lat2 - lat1).to_radians();
    let d_lon = (lon2 - lon1).to_radians();
    let a = (d_lat / 2.0).sin().powi(2)
        + lat1.to_radians().cos() * lat2.to_radians().cos() * (d_lon / 2.0).sin().powi(2);
    let c = 2.0 * a.sqrt().atan2((1.0 - a).sqrt());
    R * c
}

fn find_nearest_city(lat: f64, lon: f64) -> (&'static str, f64) {
    let mut nearest_name = "Unknown";
    let mut min_dist = f64::INFINITY;

    for city in FRENCH_CITIES {
        let dist = haversine_distance(lat, lon, city.lat, city.lon);
        if dist < min_dist {
            min_dist = dist;
            nearest_name = city.name;
        }
    }

    (nearest_name, min_dist)
}

fn quadkey_to_lat_lon(quadkey: &str) -> (f64, f64) {
    let mut x = 0u32;
    let mut y = 0u32;
    let z = quadkey.len();

    for (i, c) in quadkey.chars().enumerate() {
        let bit = z - i;
        let mask = 1 << (bit - 1);
        match c {
            '1' => x |= mask,
            '2' => y |= mask,
            '3' => {
                x |= mask;
                y |= mask;
            }
            _ => {}
        }
    }

    let n = 2_u32.pow(z as u32) as f64;
    let lon = (x as f64 + 0.5) / n * 360.0 - 180.0;
    let lat_rad = std::f64::consts::PI * (1.0 - 2.0 * (y as f64 + 0.5) / n);
    let lat = (lat_rad.sinh().atan()) * 180.0 / std::f64::consts::PI;

    (lat, lon)
}

#[derive(Debug)]
struct Cluster {
    city_name: String,
    count: i64,
    min_lat: f64,
    max_lat: f64,
    min_lon: f64,
    max_lon: f64,
    quadkeys: Vec<(String, i64)>,
}

pub fn check_database(db_path: &str) -> Result<()> {
    let conn = Connection::open(db_path)?;

    let total: i64 = conn.query_row("SELECT COUNT(*) FROM quadtiles", [], |row| row.get(0))?;

    // FIX: Use COALESCE to handle NULL from SUM on empty table
    let total_messages: i64 =
        conn.query_row("SELECT COALESCE(SUM(count), 0) FROM quadtiles", [], |row| {
            row.get(0)
        })?;

    println!("📊 Database Statistics");
    println!("   Database: {}", db_path);
    println!("   Total quadtiles: {}", total);
    println!("   Total messages: {}", total_messages);
    println!();

    if total == 0 {
        println!("⚠️  Database is empty. Run 'parse' command first.");
        return Ok(());
    }

    let mut stmt = conn.prepare("SELECT quadkey, count FROM quadtiles")?;
    let all_tiles: Vec<(String, i64)> = stmt
        .query_map([], |row| Ok((row.get(0)?, row.get(1)?)))?
        .filter_map(Result::ok)
        .collect();

    let mut clusters: HashMap<String, Cluster> = HashMap::new();

    for (qk, count) in &all_tiles {
        let (lat, lon) = quadkey_to_lat_lon(qk);
        let (city_name, _dist) = find_nearest_city(lat, lon);

        clusters
            .entry(city_name.to_string())
            .and_modify(|cluster| {
                cluster.count += count;
                cluster.min_lat = cluster.min_lat.min(lat);
                cluster.max_lat = cluster.max_lat.max(lat);
                cluster.min_lon = cluster.min_lon.min(lon);
                cluster.max_lon = cluster.max_lon.max(lon);
                cluster.quadkeys.push((qk.clone(), *count));
            })
            .or_insert_with(|| Cluster {
                city_name: city_name.to_string(),
                count: *count,
                min_lat: lat,
                max_lat: lat,
                min_lon: lon,
                max_lon: lon,
                quadkeys: vec![(qk.clone(), *count)],
            });
    }

    let mut sorted_clusters: Vec<_> = clusters.into_iter().collect();
    sorted_clusters.sort_by(|a, b| b.1.count.cmp(&a.1.count));

    println!("🗺️  Geographic Clusters (by nearest city):");
    println!("{:-<80}", "");

    for (idx, (_city_name, cluster)) in sorted_clusters.iter().enumerate() {
        let center_lat = (cluster.min_lat + cluster.max_lat) / 2.0;
        let center_lon = (cluster.min_lon + cluster.max_lon) / 2.0;
        let span_lat = cluster.max_lat - cluster.min_lat;
        let span_lon = cluster.max_lon - cluster.min_lon;
        let span_km = haversine_distance(
            cluster.min_lat,
            cluster.min_lon,
            cluster.max_lat,
            cluster.max_lon,
        );

        println!();
        println!("{}. 📍 {} Cluster", idx + 1, cluster.city_name);
        println!("   Messages: {}", cluster.count);
        println!("   Quadtiles: {}", cluster.quadkeys.len());
        println!("   Center: ({:.6}, {:.6})", center_lat, center_lon);
        println!(
            "   Span: {:.4}° lat x {:.4}° lon (~{:.1} km)",
            span_lat, span_lon, span_km
        );
        println!(
            "   BBox: [{:.6}, {:.6}] -> [{:.6}, {:.6}]",
            cluster.min_lon, cluster.min_lat, cluster.max_lon, cluster.max_lat
        );

        let mut top_quadkeys = cluster.quadkeys.clone();
        top_quadkeys.sort_by(|a, b| b.1.cmp(&a.1));
        println!("   Top quadkeys:");
        for (qk, count) in top_quadkeys.iter().take(5) {
            let (lat, lon) = quadkey_to_lat_lon(qk);
            println!(
                "      {} -> ({:.6}, {:.6}) - {} msgs",
                &qk[..qk.len().min(20)],
                lat,
                lon,
                count
            );
        }
    }

    println!();
    println!("{:-<80}", "");

    println!();
    println!("📈 Summary:");
    println!(
        "   Detected {} geographic cluster(s)",
        sorted_clusters.len()
    );

    if sorted_clusters.len() == 1 {
        println!("   ✅ Single location dataset");
    } else {
        println!("   🚗 Multi-location dataset (possible trajectory)");
    }

    println!();
    println!("🔝 Top 10 quadkeys (global):");
    println!(
        "{:<22} {:>12} {:>12} {:>10} {:>15}",
        "Quadkey", "Latitude", "Longitude", "Count", "Nearest City"
    );
    println!("{:-<80}", "");

    let mut all_sorted = all_tiles.clone();
    all_sorted.sort_by(|a, b| b.1.cmp(&a.1));

    for (qk, count) in all_sorted.iter().take(10) {
        let (lat, lon) = quadkey_to_lat_lon(qk);
        let (city, dist) = find_nearest_city(lat, lon);
        let dist_str = if dist < 1.0 {
            format!("{}m", (dist * 1000.0) as i32)
        } else {
            format!("{:.1}km", dist)
        };
        println!(
            "{:<22} {:>12.6} {:>12.6} {:>10} {:>10} ({})",
            &qk[..qk.len().min(22)],
            lat,
            lon,
            count,
            city,
            dist_str
        );
    }

    Ok(())
}
