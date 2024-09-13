use std::env;

use headers::{
  authorization::{Basic, Credentials},
  HeaderValue,
};
use rocket::{
  http::Status,
  outcome::Outcome,
  request::{self, FromRequest},
  Request,
};

use super::PoucheError;

pub(crate) struct Auth;

#[async_trait]
impl<'r> FromRequest<'r> for Auth {
  type Error = PoucheError;

  async fn from_request(request: &'r Request<'_>) -> request::Outcome<Auth, Self::Error> {
    match (env::var("WEBHOOK_USERNAME"), env::var("WEBHOOK_PASSWORD")) {
      (Ok(username), Ok(password)) => request
        .headers()
        .get("authorization")
        .next()
        .and_then(|value| HeaderValue::from_str(value).ok())
        .and_then(|value| Basic::decode(&value))
        .and_then(|credentials| match (credentials.username(), credentials.password()) {
          (u, p) if u == username && p == password => Some(Outcome::Success(Auth)),
          _ => None,
        })
        .unwrap_or(Outcome::Error((Status::Unauthorized, PoucheError::InvalidCredentials))),

      _ => Outcome::Success(Auth),
    }
  }
}
