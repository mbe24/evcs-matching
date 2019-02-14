import * as ACTION from '../actions/actionTypes';

const initialState = {};

export default (state = initialState, action) => {
  switch (action.type) {
    case ACTION.SET_REQUEST_ACTIVE:
      return {
        ...state,
        request: action.payload.request,
        active: action.payload.active
      };
    default:
      return state;
  }
};
