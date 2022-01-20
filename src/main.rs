#[macro_use]
extern crate rocket;

mod auth;
mod push;
mod webhook;

use std::io;

use pyo3::prelude::*;
use thiserror::Error;

#[derive(Error, Debug)]
pub(crate) enum PoucheError {
  #[error("invalid credentials")]
  InvalidCredentials,
  #[error("message adapter missing: {0}")]
  AdapterMissing(#[from] io::Error),
  #[error("message adapter failed: {0}")]
  AdapterFailed(String),
  #[error("invalid payload: {0}")]
  InvalidPayload(#[from] serde_json::error::Error),
  #[error("failed sending message: {0}")]
  SendFailed(#[from] fcm::FcmError),
}

#[pyclass]
#[derive(Debug, Clone)]
struct Message {
  title: String,
  body: String,
  banner: Option<String>,
  color: Option<String>
}

#[pymethods]
impl Message {
  #[new]
  fn new(title: String, body: String, banner: Option<String>, color: Option<String>) -> Self {
    Message { title, body, banner, color }
  }
}

#[launch]
fn rocket() -> _ {
  rocket::build().mount("/", routes![webhook::webhook])
}
