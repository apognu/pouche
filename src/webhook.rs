use std::fs;

use pyo3::{prelude::*, types::PyTuple};
use rocket::{http::Status, response::status::Custom};

use crate::{auth::Auth, push, Message, PoucheError};

#[post("/webhook/<id>", format = "application/json", data = "<payload>")]
pub(crate) async fn webhook(_auth: Auth, id: &str, payload: &str) -> Result<(), Custom<()>> {
  match transform_payload(id, payload) {
    Ok((topic, message)) => {
      push::send(&topic, message).await.map_err(|_| Custom(Status::Ok, ()))?;

      Ok(())
    }

    Err(err) => {
      error!("{}", err);

      Err(Custom(Status::Ok, ()))
    }
  }
}

fn transform_payload(id: &str, payload: &str) -> Result<(String, Message), PoucheError> {
  if !id.chars().all(char::is_alphanumeric) {
    return Err(PoucheError::AdapterFailed("wrong adapter name".to_string()));
  }

  let file = format!("{}.py", id);
  let code = fs::read_to_string(&file).map_err(PoucheError::from)?;

  let (topic, message) = Python::with_gil(|py| {
    let module = PyModule::from_code_bound(py, &code, &file, &file)?;
    module.add_class::<Message>()?;

    let topic: String = module.getattr("topic")?.call1(PyTuple::new_bound(py, &[payload]))?.extract()?;
    let message = module.getattr("transform")?.call1(PyTuple::new_bound(py, &[payload]))?.extract()?;

    PyResult::Ok((topic, message))
  })
  .map_err(|err| PoucheError::AdapterFailed(err.to_string()))?;

  Ok((topic, message))
}
