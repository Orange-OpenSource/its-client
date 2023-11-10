use criterion::{criterion_group, criterion_main, Criterion};
use libits::mobility::position::{position_from_degrees, vincenty_destination};

fn bench_vincenty_destination(c: &mut Criterion) {
    let position = position_from_degrees(48.62519582726, 2.24150938995, 0.);
    let bearing = 90f64;
    let distance = 100.;

    c.bench_function("Vincenty destination 100m 90deg", |b| {
        b.iter(|| vincenty_destination(&position, bearing, distance))
    });
}

criterion_group!(benches, bench_vincenty_destination);
criterion_main!(benches);
