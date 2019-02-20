import * as ACTION from '../actions/actionTypes';

const initialState = {};

export default (state = initialState, action) => {
  switch (action.type) {
    case ACTION.FETCH_RESERVATION:
      return {
        ...state,
        reservations: action.payload
      };
    case ACTION.FETCH_RESERVATION_RESET:
      return {
        ...state,
        reservations: null
      };
    case ACTION.FETCH_RESERVATION_ERROR:
      return state;
    case ACTION.UPDATE_RESERVATION:
      return {
        ...state,
        paymentUpdate: { ...action.payload }
      };
    case ACTION.UPDATE_RESERVATION_RESET:
      return {
        ...state,
        paymentUpdate: null
      };
    default:
      return state;
  }
};
