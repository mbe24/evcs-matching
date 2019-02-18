import * as ACTION from './actionTypes';

// status for reservations might change, so always get all
export const fetchReservations = lastId => dispatch =>
  fetch('/app/api/v1/reservations/load?lastId=' + lastId)
    .then(response => response.json())
    .then(reservations => {
      dispatch({ type: ACTION.FETCH_RESERVATIONS, payload: reservations });
    })
    .catch(err => {
      dispatch({ type: ACTION.FETCH_RESERVATIONS_ERROR, payload: err });
    });

// TODO update code
// https://stackoverflow.com/questions/630453/put-vs-post-in-rest
// https://stackoverflow.com/questions/35878351/put-request-in-spring-mvc

export const updateReservation = (id, operation) => dispatch => {
  fetch('/app/api/v1/reservations/' + id + '?op=' + operation, {
    method: 'put',
    headers: {
      'Content-Type': 'application/json'
    }
  })
    .then(_ => {
      dispatch({
        type: ACTION.UPDATE_RESERVATION,
        payload: { id, op: operation }
      });

      dispatch({
        type: ACTION.UPDATE_RESERVATION_RESET
      });
    })
    .catch(err => {
      dispatch({ type: ACTION.UPDATE_RESERVATION_ERROR, payload: err });
    });
};
