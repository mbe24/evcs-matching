import * as ACTION from '../actions/actionTypes';

const initialState = { update: {} };

export default (state = initialState, action) => {
  switch (action.type) {
    case ACTION.FETCH_RESERVATIONS:
      return {
        ...state,
        reservations: action.payload
      };
    case ACTION.FETCH_RESERVATIONS_ERROR:
      return {
        ...state,
        update: {}
      };
    case ACTION.UPDATE_RESERVATION:
      let update = { id: action.payload.id, op: action.payload.op };
      return { ...state, update: update };
    case ACTION.UPDATE_RESERVATION_RESET:
      return {
        ...state,
        update: {}
      };
    default:
      return {
        ...state,
        update: {}
      };
  }
};
