import * as ACTION from './actionTypes';

export const fetchRequests = lastRequestId => dispatch =>
  fetch('/app/api/v1/requests/load?lastId=' + lastRequestId)
    .then(response => response.json())
    .then(requests => {
      dispatch({ type: ACTION.FETCH_REQUEST, payload: requests });
    })
    .catch(err => {
      dispatch({ type: ACTION.FETCH_REQUEST_ERROR, payload: err });
    });
