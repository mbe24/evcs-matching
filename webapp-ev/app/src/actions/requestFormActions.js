import * as ACTION from './actionTypes';

export const submitRequest = request => dispatch => {
  dispatch({ type: ACTION.SUBMIT_REQUEST_START, payload: { isLoading: true } });

  fetch('/app/api/v1/requests/create', {
    method: 'post',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(request)
  })
    .then(_ => {
      dispatch({ type: ACTION.SUBMIT_REQUEST, payload: { isLoading: false } });
    })
    .catch(err => {
      dispatch({
        type: ACTION.SUBMIT_REQUEST_ERROR,
        payload: { error: err, isLoading: false }
      });
    });
};
