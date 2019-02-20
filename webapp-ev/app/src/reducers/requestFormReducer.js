import * as ACTION from '../actions/actionTypes';

const initialState = { isLoading: false };

export default (state = initialState, action) => {
  switch (action.type) {
    case ACTION.SUBMIT_REQUEST_START:
      return { ...state, isLoading: action.payload.isLoading };
    case ACTION.SUBMIT_REQUEST:
      return { ...state, isLoading: action.payload.isLoading };
    default:
      return state;
  }
};
