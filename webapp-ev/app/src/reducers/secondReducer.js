import * as ACTION from '../actions/actionTypes';

const initialState = {
  hour: '00',
  minute: '00',
  second: '00'
};

export default (state = initialState, action) => {
  switch (action.type) {
    case ACTION.FETCH_SECOND:
      return { ...state, second: action.payload };
    case ACTION.FETCH_TIME:
      return { ...state, hour: action.payload.second };
    default:
      return state;
  }
};
