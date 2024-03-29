import * as ACTION from './actionTypes';

export const submitOffer = (offer, requestId) => dispatch => {
  dispatch({ type: ACTION.SUBMIT_OFFER_START, payload: { isLoading: true } });

  fetch('/app/api/v1/offers/create/r/' + requestId, {
    method: 'post',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(offer)
  })
    .then(_ => {
      dispatch({ type: ACTION.SUBMIT_OFFER, payload: { isLoading: false } });
    })
    .catch(err => {
      dispatch({
        type: ACTION.SUBMIT_OFFER_ERROR,
        payload: { error: err, isLoading: false }
      });
    });
};
