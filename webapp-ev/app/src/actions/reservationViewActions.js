import * as ACTION from './actionTypes';

export const fetchReservations = lastId => dispatch =>
  fetch('/app/api/v1/reservations/load?lastId=' + lastId)
    .then(response => response.json())
    .then(reservation => {
      dispatch({ type: ACTION.FETCH_RESERVATION, payload: reservation });

      dispatch({
        type: ACTION.FETCH_RESERVATION_RESET,
        payload: null
      });
    })
    .catch(err => {
      dispatch({ type: ACTION.FETCH_RESERVATION_ERROR, payload: err });
    });

// TODO update code
// https://stackoverflow.com/questions/630453/put-vs-post-in-rest
// https://stackoverflow.com/questions/35878351/put-request-in-spring-mvc

export const updateReservation = (id, payment) => dispatch => {
  fetch('/app/api/v1/reservations/' + id + '?payment=' + payment, {
    method: 'put',
    headers: {
      'Content-Type': 'application/json'
    }
  })
    .then(response => response.json())
    .then(reservation => {
      dispatch({
        type: ACTION.UPDATE_RESERVATION,
        payload: { id, payment }
      });

      dispatch({
        type: ACTION.UPDATE_RESERVATION_RESET,
        payload: null
      });
    })
    .catch(err => {
      dispatch({ type: ACTION.UPDATE_RESERVATION_ERROR, payload: err });
    });
};
