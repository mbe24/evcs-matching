import * as ACTION from './actionTypes';

export const fetchTime = () => dispatch =>
  fetch('/app/api/v1/time')
    .then(response => response.json())
    .then(time => {
      console.log('Sucessfully fetched time...');
      dispatch({ type: ACTION.FETCH_HOUR, payload: time.hour });
      dispatch({ type: ACTION.FETCH_MINUTE, payload: time.minute });
      dispatch({ type: ACTION.FETCH_SECOND, payload: time.second });
    })
    .catch(err => {
      console.log('Error while fetching time...');
      dispatch({ type: ACTION.FETCH_HOUR_ERROR, payload: err });
      dispatch({ type: ACTION.FETCH_MINUTE_ERROR, payload: err });
      dispatch({ type: ACTION.FETCH_SECOND_ERROR, payload: err });
    });

// createPollingAction() returns a thunk which continually polls, once invoked.
// subsequent invocations will cancel the previous poll queue.
const createPoller = (interval, initialDelay) => {
  let timeoutId = null;
  let poller = () => {};
  return fn => {
    window.clearTimeout(timeoutId);
    poller = () => {
      timeoutId = window.setTimeout(poller, interval);
      return fn();
    };
    if (initialDelay) {
      return (timeoutId = window.setTimeout(poller, interval));
    }
    return poller();
  };
};

const createPollingAction = (action, interval, initialDelay) => {
  const poll = createPoller(action, initialDelay);
  return () => (dispatch, getState) => poll(() => action(dispatch, getState));
};

export const fetchTimePoll = createPollingAction(dispatch => {
  fetch('/app/api/v1/time')
    .then(response => response.json())
    .then(time => {
      console.log('Sucessfully fetched time...');
      dispatch({ type: ACTION.FETCH_HOUR, payload: time.hour });
      dispatch({ type: ACTION.FETCH_MINUTE, payload: time.minute });
      dispatch({ type: ACTION.FETCH_SECOND, payload: time.second });
    })
    .catch(err => {
      console.log('Error while fetching time...');
      dispatch({ type: ACTION.FETCH_HOUR_ERROR, payload: err });
      dispatch({ type: ACTION.FETCH_MINUTE_ERROR, payload: err });
      dispatch({ type: ACTION.FETCH_SECOND_ERROR, payload: err });
    });
}, 5000);
