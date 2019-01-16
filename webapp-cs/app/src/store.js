import { applyMiddleware, createStore } from 'redux';
import thunk from 'redux-thunk';
import reducer from './reducers';

// in order to use promises in action creators
// see: https://stackoverflow.com/q/36577510/1809463
// import promise from 'redux-promise-middleware';
//const middleware = applyMiddleware(promise(), thunk);

// maybe use add logger to middleware: https://github.com/LogRocket/redux-logger#install
const middleware = applyMiddleware(thunk);
export default createStore(reducer, middleware);
