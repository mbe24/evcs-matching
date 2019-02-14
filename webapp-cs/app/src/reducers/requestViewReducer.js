import * as ACTION from '../actions/actionTypes';

const initialState = {};

export default (state = initialState, action) => {
  switch (action.type) {
    case ACTION.FETCH_REQUEST:
      return {
        ...state,
        requests: action.payload
      };
    case ACTION.FETCH_REQUEST_ERROR:
      return state;
    default:
      return state;
  }
};
