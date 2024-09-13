use std::{collections::HashMap, env};

use fcm::message::{Message as FcmMessage, Target};

use crate::{Message, PoucheError};

pub(crate) async fn send(topic: &str, message: Message) -> Result<(), PoucheError> {
  let sa = env::var("SERVICE_ACCOUNT_FILE").unwrap();
  let topic = format!("{}", topic);

  let client = fcm::FcmClient::builder().service_account_key_json_path(sa).build().await.unwrap();

  let mut data = HashMap::new();

  data.insert("title", message.title.clone());
  data.insert("body", message.body.clone());

  if let Some(banner) = message.banner {
    data.insert("banner", banner);
  }

  if let Some(color) = message.color {
    data.insert("color", color);
  }

  let data = serde_json::to_value(data).unwrap();

  let push = FcmMessage {
    target: Target::Topic(topic),
    data: Some(data),
    notification: None,
    android: None,
    apns: None,
    webpush: None,
    fcm_options: None,
  };

  client.send(push).await?;

  Ok(())
}
