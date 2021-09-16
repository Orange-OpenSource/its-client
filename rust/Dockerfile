FROM rust:buster as builder

LABEL description="Rust builder to compile"
LABEL maintainer="frederic.gardes@orange.com"
LABEL authors=["frederic.gardes@orange.com"]
LABEL url="https://github.com/tigroo31/its-client"

RUN rustc --version && cargo --version

WORKDIR /usr/src/app

COPY libits-client/src libits-client/src
COPY libits-client/Cargo.toml libits-client/Cargo.toml
COPY libits-copycat/src libits-copycat/src
COPY libits-copycat/Cargo.toml libits-copycat/Cargo.toml
COPY its-client/src its-client/src
COPY its-client/Cargo.toml its-client/Cargo.toml

COPY Cargo.toml .

RUN cargo build --verbose --all && \
    cargo test --verbose --all && \
    cargo build --release

FROM debian:buster

LABEL description="Rust ITS client"
LABEL maintainer="frederic.gardes@orange.com"
LABEL authors=["frederic.gardes@orange.com"]
LABEL url="https://github.com/tigroo31/its-client"

# Install curl for healthcheck
RUN apt-get update && \
    apt-get install --no-install-recommends -y \
        curl=7.* \
        procps=2:* && \
    rm -rf /var/lib/apt/lists/*

# Create app directory
WORKDIR /usr/src/app

RUN groupadd -r itsclient && useradd --no-log-init -r -g itsclient itsclient && \
    mkdir /usr/src/app/log && \
    printf "Version 0.2.3\n" > /usr/src/app/log/RELEASE

COPY --from=builder --chown=itsclient:itsclient /usr/src/app/target/release/its-client .

COPY docker-entrypoint.sh .

RUN chown -R itsclient:itsclient /usr/src/app && chmod -R ug+rwx /usr/src/app

# no parameters today, just to check de lint
CMD [""]

USER itsclient

ENTRYPOINT ["/usr/src/app/docker-entrypoint.sh"]

HEALTHCHECK CMD ./its-client --help || exit 1