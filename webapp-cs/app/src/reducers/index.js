import { combineReducers } from 'redux';
import offerFormReducer from './offerFormReducer';
import requestViewReducer from './requestViewReducer';
import offerViewReducer from './offerViewReducer';

// it is possible to name reducers
export default combineReducers({
  offerFormReducer: offerFormReducer,
  requestViewReducer: requestViewReducer,
  offerViewReducer: offerViewReducer
});
