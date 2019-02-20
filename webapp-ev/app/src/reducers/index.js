import { combineReducers } from 'redux';
import requestFormReducer from './requestFormReducer';
import requestViewReducer from './requestViewReducer';
import offerViewReducer from './offerViewReducer';
import reservationViewReducer from './reservationViewReducer';

// it is possible to name reducers
export default combineReducers({
  requestFormReducer: requestFormReducer,
  requestViewReducer: requestViewReducer,
  offerViewReducer: offerViewReducer,
  reservationViewReducer: reservationViewReducer
});
