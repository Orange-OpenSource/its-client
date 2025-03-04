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

use criterion::{Criterion, criterion_group, criterion_main};
use libits::mobility::position::{
    haversine_destination, position_from_degrees, vincenty_destination,
};

fn bench_vincenty_destination(c: &mut Criterion) {
    let position = position_from_degrees(48.62519582726, 2.24150938995, 0.);
    let bearing = 90f64;
    let distance = 100.;

    c.bench_function("Vincenty destination 100m 90deg", |b| {
        b.iter(|| vincenty_destination(&position, bearing, distance))
    });
}

fn bench_haversine_destination(c: &mut Criterion) {
    let position = position_from_degrees(48.62519582726, 2.24150938995, 0.);
    let bearing = 90f64;
    let distance = 100.;

    c.bench_function("Haversine destination 100m 90deg", |b| {
        b.iter(|| haversine_destination(&position, bearing, distance))
    });
}

criterion_group!(
    benches,
    bench_vincenty_destination,
    bench_haversine_destination
);
criterion_main!(benches);
