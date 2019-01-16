import { combineReducers } from 'redux';

import minuteReducer from './minuteReducer';
import hourReducer from './hourReducer';
import secondReducer from './secondReducer';

// it is possible to name reducers
export default combineReducers({
  hourReducer: hourReducer,
  minuteReducer: minuteReducer,
  secondReducer: secondReducer
});
