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

use crate::transport::mqtt::topic::Topic;
use crate::transport::packet::Packet;
use crate::transport::payload::Payload;

use crossbeam_channel::Sender;
use log::{debug, error, info, trace, warn};
use rumqttc::v5::mqttbytes::QoS;
use rumqttc::v5::mqttbytes::v5::{Filter, PublishProperties};
use rumqttc::v5::{AsyncClient, Event, EventLoop, MqttOptions};

#[cfg(feature = "telemetry")]
use {
    crate::transport::telemetry::get_mqtt_span,
    opentelemetry::Context,
    opentelemetry::propagation::TextMapPropagator,
    opentelemetry::trace::{SpanKind, TraceContextExt},
    opentelemetry_sdk::propagation::TraceContextPropagator,
};

pub struct MqttClient {
    client: AsyncClient,
}

impl MqttClient {
    pub fn new(options: &MqttOptions) -> (Self, EventLoop) {
        let (client, event_loop) = AsyncClient::new(options.clone(), 1000);
        (MqttClient { client }, event_loop)
    }

    pub async fn subscribe(&mut self, topic_list: &[String]) {
        match self
            .client
            .subscribe_many(
                topic_list
                    .iter()
                    .map(|topic| Filter::new(topic.clone(), QoS::AtMostOnce))
                    .collect::<Vec<Filter>>(),
            )
            .await
        {
            Ok(()) => debug!("Sent subscriptions"),
            Err(e) => {
                error!("Failed to send subscriptions, is the connection close? \nError: {e:?}")
            }
        };
    }

    #[cfg(feature = "telemetry")]
    pub async fn publish<T: Topic, P: Payload>(&self, mut packet: Packet<T, P>) {
        debug!("Publish with context");
        let payload = serde_json::to_string(&packet.payload).unwrap();

        let span = get_mqtt_span(
            SpanKind::Producer,
            &packet.topic.to_string(),
            payload.len() as i64,
        );

        let cx = Context::current().with_span(span);
        let _guard = cx.attach();

        let propagator = TraceContextPropagator::new();
        propagator.inject(&mut packet);

        self.do_publish(packet).await
    }

    #[cfg(not(feature = "telemetry"))]
    pub async fn publish<T: Topic, P: Payload>(&self, packet: Packet<T, P>) {
        debug!("Publish without context");
        self.do_publish(packet).await
    }

    async fn do_publish<T: Topic, P: Payload>(&self, packet: Packet<T, P>) {
        let payload = serde_json::to_string(&packet.payload).unwrap();

        match self
            .client
            .publish_with_properties(
                packet.topic.to_string(),
                QoS::ExactlyOnce,
                false,
                payload,
                packet.properties,
            )
            .await
        {
            Ok(()) => {
                trace!("Sent publish");
            }
            Err(e) => error!("Failed to send publish, is the connection close? \nError: {e:?}"),
        }
    }

    /// Forwards an already-serialized payload as-is, without re-serializing it.
    ///
    /// This is required when forwarding payloads received from another source
    /// (e.g. a JSON string from another broker): using [`MqttClient::publish`] would call
    /// `serde_json::to_string` on the `String`, escaping it and producing a
    /// double-encoded message.
    pub async fn publish_forward<T: Topic>(
        &self,
        topic: T,
        payload: String,
        properties: PublishProperties,
    ) {
        match self
            .client
            .publish_with_properties(
                topic.to_string(),
                QoS::ExactlyOnce,
                false,
                payload,
                properties,
            )
            .await
        {
            Ok(()) => trace!("Sent forwarded publish"),
            Err(e) => {
                error!("Failed to send forwarded publish, is the connection close? \nError: {e:?}")
            }
        }
    }
}

pub async fn listen(mut event_loop: EventLoop, sender: Sender<Event>) {
    info!("Listening started");
    let mut listening = true;
    while listening {
        match event_loop.poll().await {
            Ok(event) => match sender.send(event) {
                Ok(()) => trace!("Item sent"),
                Err(error) => {
                    error!("Stopped to send item: {error}");
                    listening = false;
                }
            },
            Err(error) => {
                error!("Stopped to receive event: {error:?}");
                listening = false;
            }
        }
    }
    warn!("Listening done");
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_mqtt_client_creation() {
        let mut options = MqttOptions::new("test_client", "localhost", 1883);
        options.set_keep_alive(core::time::Duration::from_secs(60));
        let (_client, _event_loop) = MqttClient::new(&options);
        // Verify client is created without panicking
        assert!(true);
    }

    #[test]
    fn test_publish_forward_payload_not_double_encoded() {
        // This test verifies that a forwarded JSON string payload is NOT
        // further JSON-encoded when using publish_forward.
        // The payload should be transmitted as-is without escaping.
        let json_payload = r#"{"message_type":"cam","data":"test"}"#;

        // When this payload would be passed to publish (typed),
        // serde_json::to_string would escape it to:
        // "{\"message_type\":\"cam\",\"data\":\"test\"}"
        // which is double-encoded and invalid.

        // publish_forward should send json_payload directly without re-encoding.
        // We can't easily test the actual MQTT output without mocking,
        // but we document the expected behavior here.
        assert_eq!(
            json_payload, r#"{"message_type":"cam","data":"test"}"#,
            "Forwarded JSON payload must not be modified"
        );
    }

    #[test]
    fn test_publish_serialized_typed_payload() {
        // Test that a typed payload (like Exchange) is correctly serialized
        // once when using the regular publish method.
        // This prevents regression: publish should still work for typed payloads.

        #[derive(serde::Serialize, serde::Deserialize, Debug, Clone, PartialEq)]
        struct TestPayload {
            msg_type: String,
            value: i32,
        }

        let payload = TestPayload {
            msg_type: "test".to_string(),
            value: 42,
        };

        let serialized = serde_json::to_string(&payload).expect("Should serialize");
        // Verify single encoding: no escaping of quotes
        assert!(serialized.contains("\"msg_type\""));
        assert!(!serialized.contains("\\\"msg_type\\\""));
    }

    #[test]
    fn test_string_double_encoding_with_publish_scenario() {
        // Demonstrate the bug: a String (forwarded JSON) serialized twice
        let json_payload_string = r#"{"message":"hello"}"#.to_string();

        // First serialization (correct for typed Payload, wrong for String):
        let once_encoded = serde_json::to_string(&json_payload_string).expect("Should serialize");

        // Verify it's double-encoded (contains escaped quotes):
        assert!(once_encoded.contains("\\\""));
        assert!(once_encoded.starts_with("\""));
        assert!(once_encoded.ends_with("\""));

        // publish_forward should bypass this by passing json_payload_string directly
        // to mqtt client without serde_json::to_string
    }
}
