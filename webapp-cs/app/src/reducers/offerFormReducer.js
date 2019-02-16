import * as ACTION from '../actions/actionTypes';

const initialState = { isLoading: false };

export default (state = initialState, action) => {
  switch (action.type) {
    case ACTION.SET_REQUEST_ACTIVE:
      return {
        ...state,
        request: action.payload.request,
        active: action.payload.active
      };
    case ACTION.SUBMIT_OFFER_START:
      return { ...state, isLoading: action.payload.isLoading };
    case ACTION.SUBMIT_OFFER:
      return { ...state, isLoading: action.payload.isLoading };
    default:
      return state;
  }
};
