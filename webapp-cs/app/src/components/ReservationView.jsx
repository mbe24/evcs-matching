import React from 'react';
import { connect } from 'react-redux';
import Button from 'react-bootstrap/lib/Button';
import Table from 'react-bootstrap/lib/Table';
import './ReservationView.css';
import {
  fetchReservations,
  updateReservation
} from '../actions/reservationViewActions';

// http://allenfang.github.io/react-bootstrap-table/index.html
// https://react-bootstrap.netlify.com/components/table/
// https://react-bootstrap.github.io/components/modal/

class ReservationView extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      lastId: -1,
      activeItem: -1,
      items: []
    };

    this.handleClick = this.handleClick.bind(this);
    this.handleLoad = this.handleLoad.bind(this);
    this.handleAction = this.handleAction.bind(this);
    this.createTableEntries = this.createTableEntries.bind(this);
  }

  // https://daveceddia.com/where-fetch-data-redux/
  componentWillReceiveProps(nextProps) {
    let reservations = nextProps.reservations;

    let items = this.state.items;
    if (reservations != null && reservations.length !== 0) {
      items = reservations;
    }

    let update = nextProps.update;
    if (update != null && Object.keys(update).length > 0) {
      let status = update.op === 'ACCEPT' ? 'ACCEPTED' : 'REJECTED';
      items.find(item => item.id === update.id).status = status;
    }

    this.setState({
      ...this.state,
      items: items
    });
  }

  handleClick(event) {
    let id = event.currentTarget.dataset.id;
    if (id === this.state.activeItem) id = -1;
    this.setState({ ...this.state, activeItem: id });
  }

  handleLoad(event) {
    this.props.fetchReservations(this.state.lastId);
  }

  handleAction(event) {
    let op = event.currentTarget.dataset.action;
    let reservation = this.state.items[this.state.activeItem];
    this.props.updateReservation(reservation.id, op);
  }

  createTableEntries(items) {
    let entries = [];
    items.forEach((item, i) => {
      let isActive = i.toString() === this.state.activeItem.toString();

      entries.push(
        <tr
          className={isActive ? 'info' : ''}
          key={i}
          data-id={i}
          onClick={this.handleClick}
        >
          <td>{item.requestId}</td>
          <td>{item.offerId}</td>
          <td>{item.price}</td>
          <td>{item.payment}</td>
          <td>{item.status}</td>
        </tr>
      );
    });

    return entries;
  }

  render() {
    let enableActions = false;
    if (this.state.activeItem !== -1) {
      let reservation = this.state.items[this.state.activeItem];
      enableActions = 'OPEN' === reservation.status;
    }

    return (
      <div className="ReservationView col-sm-6">
        <div className="row">
          <div className="pull-left">
            <h3>Reservations</h3>
          </div>
        </div>

        <div className="row">
          <div className="pull-left">
            <Button
              bsStyle="primary"
              bsSize="sm"
              type="button"
              onClick={this.handleLoad}
            >
              Load
            </Button>

            <span className="margin-left">
              <Button
                disabled={!enableActions}
                key="acpt"
                data-action="ACCEPT"
                bsStyle="success"
                bsSize="sm"
                type="button"
                onClick={this.handleAction}
              >
                Accept
              </Button>
            </span>

            <span className="margin-left">
              <Button
                disabled={!enableActions}
                key="rjct"
                data-action="REJECT"
                bsStyle="danger"
                bsSize="sm"
                type="button"
                onClick={this.handleAction}
              >
                Reject
              </Button>
            </span>
          </div>
        </div>

        <div className="row">
          <Table responsive striped hover>
            <thead>
              <tr>
                <th className="text-center">Request</th>
                <th className="text-center">Offer</th>
                <th className="text-center">Price</th>
                <th className="text-center">Payment</th>
                <th className="text-center">Status</th>
              </tr>
            </thead>
            <tbody>{this.createTableEntries(this.state.items)}</tbody>
          </Table>
        </div>
      </div>
    );
  }
}

// https://redux.js.org/api/bindactioncreators
// https://stackoverflow.com/questions/38202572/understanding-react-redux-and-mapstatetoprops

const mapStateToProps = state => ({
  reservations: state.reservationViewReducer.reservations,
  update: state.reservationViewReducer.update
});

const mapDispatchToProps = dispatch => ({
  fetchReservations: lastId => dispatch(fetchReservations(lastId)),
  updateReservation: (id, operation) =>
    dispatch(updateReservation(id, operation))
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(ReservationView);
