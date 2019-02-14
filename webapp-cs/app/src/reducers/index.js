import { combineReducers } from 'redux';
import offerFormReducer from './offerFormReducer';
import requestViewReducer from './requestViewReducer';

// it is possible to name reducers
export default combineReducers({
  offerFormReducer: offerFormReducer,
  requestViewReducer: requestViewReducer
});
