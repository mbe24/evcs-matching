import React from 'react';
import { connect } from 'react-redux';
import './App.css';
import PageHeader from 'react-bootstrap/lib/PageHeader';
import RequestForm from './components/RequestForm';
import RequestView from './components/RequestView';
import OfferView from './components/OfferView';
import ReservationView from './components/ReservationView';

// or without decorator
// see: https://blog.logrocket.com/react-redux-connect-when-and-how-to-use-it-f2a1edab2013
//@connect(store => {
//  return { hour: store.hour, minute: store.minute, second: store.second };
//})
class App extends React.Component {
  componentWillMount() {
    //this.props.fetchTimePoll();
  }

  // TODO implement OfferView with techniques from TableView
  render() {
    return (
      <div className="App container">
        <PageHeader>EV Control</PageHeader>
        <div className="row">
          <RequestForm />
          <RequestView />
        </div>

        <div className="row">
          <OfferView />
          <ReservationView />
        </div>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  ...state
});

export default connect(
  mapStateToProps,
  null
)(App);
