import * as ACTION from '../actions/actionTypes';

const initialState = { requests: [], data: {} };

export default (state = initialState, action) => {
  switch (action.type) {
    case ACTION.FETCH_REQUEST:
      return {
        ...state,
        requests: action.payload
      };
    case ACTION.FETCH_OFFERS_START:
      return state;
    case ACTION.FETCH_OFFERS:
      return {
        ...state,
        latestData: {
          requestId: action.payload.requestId,
          offers: action.payload.offers
        }
      };
    case ACTION.FETCH_OFFERS_ERROR:
      return state;
    default:
      return state;
  }
};
