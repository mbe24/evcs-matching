import React from 'react';
import { connect } from 'react-redux';
import DropdownButton from 'react-bootstrap/lib/DropdownButton';
import MenuItem from 'react-bootstrap/lib/MenuItem';
import Table from 'react-bootstrap/lib/Table';
import Button from 'react-bootstrap/lib/Button';
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
      activeMenuItems: [],
      items: []
    };

    this.handleClick = this.handleClick.bind(this);
    this.handleMenuClick = this.handleMenuClick.bind(this);
    this.handleLoad = this.handleLoad.bind(this);
    this.handlePay = this.handlePay.bind(this);
    this.createTableEntries = this.createTableEntries.bind(this);
  }

  // https://daveceddia.com/where-fetch-data-redux/
  componentWillReceiveProps(nextProps) {
    let items = this.state.items;
    let paymentUpdate = nextProps.paymentUpdate;
    if (paymentUpdate) {
      let reservation = items.find(r => r.id === paymentUpdate.id);
      reservation.status = 'PAID';
      reservation.payment = paymentUpdate.payment;
    }

    let lastId = this.state.lastId;
    let newItems = [];

    let reservations = nextProps.reservations;
    if (reservations != null && reservations.length !== 0) {
      lastId = reservations[reservations.length - 1].id;
      newItems = reservations;
    }

    this.setState({
      ...this.state,
      items: [...items, ...newItems],
      lastId: lastId
    });
  }

  handleMenuClick(event) {
    let id = event.currentTarget.dataset.id;

    let menuItems = this.state.activeMenuItems;
    menuItems[this.state.activeItem] = id;

    this.setState({ ...this.state, activeMenuItems: menuItems });
  }

  handleClick(event) {
    let id = event.currentTarget.dataset.id;
    if (id === this.state.activeItem) id = -1;
    this.setState({ ...this.state, activeItem: id });
  }

  handleLoad(event) {
    this.props.fetchReservations(this.state.lastId);
  }

  handlePay(event) {
    let reservation = this.state.items[this.state.activeItem];

    let menuItems = this.state.activeMenuItems;
    let activeItem = menuItems[this.state.activeItem];
    let paymentOption = reservation.paymentOptions[activeItem];

    this.props.updateReservation(reservation.id, paymentOption);
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

  createMenuItems(ids) {
    let items = [];

    let hasActiveItem = this.state.activeItem !== -1;
    let menuItems = this.state.activeMenuItems;

    if (!hasActiveItem) return items;

    ids.forEach((id, i) => {
      let activeItem = menuItems[this.state.activeItem];
      let isActive = activeItem && activeItem.toString() === i.toString();

      items.push(
        <MenuItem
          key={i}
          data-id={i}
          eventKey={i}
          active={isActive}
          onClick={this.handleMenuClick}
        >
          {id}
        </MenuItem>
      );
    });

    return items;
  }

  render() {
    let isActive = -1 !== this.state.activeItem;
    let reservation = {};
    let paymentOptions = [];
    let isPayActive = false;

    if (isActive) {
      reservation = this.state.items[this.state.activeItem];
      isActive = reservation.status === 'ACCEPTED';
      paymentOptions = reservation.paymentOptions;

      let menuItems = this.state.activeMenuItems;
      let activeItem = menuItems[this.state.activeItem];
      isPayActive = isActive && activeItem && activeItem !== -1;
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
              <DropdownButton
                disabled={!isActive}
                id="request-dropdown"
                title="Payment"
                bsStyle="danger"
                bsSize="sm"
                pullRight={false}
              >
                {this.createMenuItems(paymentOptions)}
              </DropdownButton>
            </span>

            <span className="margin-left">
              <Button
                disabled={!isPayActive}
                bsStyle="danger"
                bsSize="sm"
                type="button"
                onClick={this.handlePay}
              >
                Pay
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
  paymentUpdate: state.reservationViewReducer.paymentUpdate
});

const mapDispatchToProps = dispatch => ({
  fetchReservations: lastId => dispatch(fetchReservations(lastId)),
  updateReservation: (id, payment) => dispatch(updateReservation(id, payment))
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(ReservationView);
