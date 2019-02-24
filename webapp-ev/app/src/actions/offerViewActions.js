import * as ACTION from './actionTypes';

// https://medium.com/hashmapinc/rest-good-practices-for-api-design-881439796dc9

export const fetchOffers = (requestId, lastOfferId) => dispatch => {
  fetch('app/api/v1/offers/r/' + requestId + '?lastId=' + lastOfferId)
    .then(response => response.json())
    .then(offers => {
      dispatch({
        type: ACTION.FETCH_OFFERS,
        payload: { requestId: requestId, offers: offers }
      });
    })
    .catch(err => {
      dispatch({ type: ACTION.FETCH_OFFERS_ERROR, payload: err });
    });
};

export const submitReservation = (requestId, offerId) => dispatch => {
  dispatch({
    type: ACTION.SUBMIT_RESERVATION_START,
    payload: { isLoading: true }
  });

  fetch('/app/api/v1/reservations/create/offer/' + offerId, {
    method: 'post',
    headers: {
      'Content-Type': 'application/json'
    },
    body: requestId
  })
    .then(_ => {
      dispatch({
        type: ACTION.SUBMIT_RESERVATION,
        payload: { isLoading: false }
      });
    })
    .catch(err => {
      dispatch({
        type: ACTION.SUBMIT_RESERVATION_ERROR,
        payload: { error: err, isLoading: false }
      });
    });
};
