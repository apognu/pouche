use std::{collections::HashMap, env};

use crate::{Message, PoucheError};

pub(crate) async fn send(topic: &str, message: Message) -> Result<(), PoucheError> {
  let key = env::var("FCM_API_KEY").unwrap();
  let topic = format!("/topics/{}", topic);

  let client = fcm::Client::new();
  let mut data = HashMap::new();

  data.insert("title", message.title);
  data.insert("body", message.body);

  if let Some(banner) = message.banner {
    data.insert("banner", banner);
  }

  let mut builder = fcm::MessageBuilder::new(&key, &topic);
  builder.data(&data)?;

  client.send(builder.finalize()).await?;

  Ok(())
}
