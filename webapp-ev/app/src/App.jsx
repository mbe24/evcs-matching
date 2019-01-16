import React from 'react';
import { connect } from 'react-redux';
import './App.css';
import { fetchTime, fetchTimePoll } from './actions/timeActions';
import PageHeader from 'react-bootstrap/lib/PageHeader';
import RequestForm from './components/RequestForm';
import OfferView from './components/OfferView';

// or without decorator
// see: https://blog.logrocket.com/react-redux-connect-when-and-how-to-use-it-f2a1edab2013
//@connect(store => {
//  return { hour: store.hour, minute: store.minute, second: store.second };
//})
class App extends React.Component {
  componentWillMount() {
    //this.props.fetchTimePoll();
  }

  fetchTime = event => {
    //this.props.fetchTime();
  };

  render() {
    return (
      <div className="App container">
        <PageHeader>EV Control</PageHeader>
        <RequestForm />
        <OfferView />
      </div>
    );
  }
}

const mapStateToProps = state => ({
  ...state
});

const mapDispatchToProps = dispatch => ({
  fetchTime: () => dispatch(fetchTime()),
  fetchTimePoll: () => dispatch(fetchTimePoll())
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(App);
