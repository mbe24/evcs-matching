import * as ACTION from '../actions/actionTypes';

const initialState = {
  hour: '00',
  minute: '00',
  second: '00'
};

export default (state = initialState, action) => {
  switch (action.type) {
    case ACTION.FETCH_MINUTE:
      return { ...state, minute: action.payload };
    case ACTION.FETCH_TIME:
      return { ...state, hour: action.payload.minute };
    default:
      return state;
  }
};
