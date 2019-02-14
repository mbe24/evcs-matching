import * as ACTION from '../actions/actionTypes';

const initialState = {
  set: false,
  request: {}
};

export default (state = initialState, action) => {
  switch (action.type) {
    case ACTION.FETCH_HOUR:
      console.log('in hour reducer');
      console.log(action);
      return { ...state, hour: action.payload };
    case ACTION.FETCH_TIME:
      console.log('in hour reducer');
      console.log(action);
      return { ...state, hour: action.payload.hour };
    default:
      return state;
  }
};
