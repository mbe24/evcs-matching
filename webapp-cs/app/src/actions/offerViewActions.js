import * as ACTION from './actionTypes';

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
