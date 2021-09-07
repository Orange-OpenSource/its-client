use crate::analyse::item::Item;
use crate::reception::exchange::Exchange;
use log::{debug, error, info, trace, warn};
use rumqttc::{AsyncClient, Event, EventLoop, MqttOptions, QoS, SubscribeFilter};

pub(crate) struct Client {
    client: AsyncClient,
}

impl<'client> Client {
    pub fn new(
        mqtt_host: &str,
        mqtt_port: u16,
        mqtt_client_id: &str,
        mqtt_username: Option<&str>,
        mqtt_password: Option<&str>,
    ) -> (Self, EventLoop) {
        let mqtt_options = orange_broker(
            mqtt_host,
            mqtt_port,
            mqtt_client_id,
            mqtt_username,
            mqtt_password,
        );
        let (client, event_loop) = AsyncClient::new(mqtt_options, 1000);
        (Client { client }, event_loop)
    }

    pub async fn subscribe(&mut self, topic_list: Vec<String>) {
        match self
            .client
            .subscribe_many(
                topic_list
                    .iter()
                    // assumed clone: no move into functional treatment
                    .map(|topic| SubscribeFilter::new(topic.clone(), QoS::AtMostOnce))
                    .collect::<Vec<SubscribeFilter>>(),
            )
            .await
        {
            Ok(()) => debug!("sent subscriptions"),
            Err(e) => error!(
                "failed to send subscriptions, is the connection close? \nError: {:?}",
                e
            ),
        };
    }

    pub async fn publish(&self, item: Item<Exchange>) {
        let payload = serde_json::to_string(&item.reception).unwrap();
        match self
            .client
            .publish(item.topic.to_string(), QoS::ExactlyOnce, false, payload)
            .await
        {
            Ok(()) => {
                trace!("sent publish");
            }
            Err(e) => error!(
                "failed to send publish, is the connection close? \nError: {:?}",
                e
            ),
        };
    }
}

fn orange_broker(
    mqtt_host: &str,
    mqtt_port: u16,
    mqtt_client_id: &str,
    mqtt_username: Option<&str>,
    mqtt_password: Option<&str>,
) -> MqttOptions {
    let mut mqttoptions = MqttOptions::new(mqtt_client_id, mqtt_host, mqtt_port);
    mqttoptions.set_keep_alive(5);
    match mqtt_username {
        None => {}
        Some(username) => match mqtt_password {
            None => error!("username is given but password is not: specify the password"),
            Some(password) => {
                mqttoptions.set_credentials(username.to_owned(), password.to_owned());
            }
        },
    }
    mqttoptions
}

pub async fn listen(mut event_loop: EventLoop, sender: std::sync::mpsc::Sender<Event>) {
    info!("listening started");
    let mut listening = true;
    while listening {
        match event_loop.poll().await {
            Ok(event) => match sender.send(event) {
                Ok(()) => trace!("item sent"),
                Err(error) => {
                    error!("stopped to send item: {}", error);
                    listening = false;
                }
            },
            Err(error) => {
                error!("stopped to receive event: {:?}", error);
                listening = false;
            }
        }
    }
    warn!("listening done");
}
