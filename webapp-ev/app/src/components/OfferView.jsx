import React from 'react';
import { connect } from 'react-redux';
import Button from 'react-bootstrap/lib/Button';
import DropdownButton from 'react-bootstrap/lib/DropdownButton';
import MenuItem from 'react-bootstrap/lib/MenuItem';
import Table from 'react-bootstrap/lib/Table';
import './OfferView.css';
import { fetchOffers, submitReservation } from '../actions/offerViewActions';

// http://allenfang.github.io/react-bootstrap-table/index.html
// https://react-bootstrap.netlify.com/components/table/
// https://react-bootstrap.github.io/components/modal/

class OfferView extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      activeMenuItem: -1,
      requests: [],
      data: []
    };

    this.handleMenuClick = this.handleMenuClick.bind(this);
    this.handleClick = this.handleClick.bind(this);
    this.handleLoad = this.handleLoad.bind(this);
    this.handleReserve = this.handleReserve.bind(this);
    this.createTableEntries = this.createTableEntries.bind(this);
  }

  // https://daveceddia.com/where-fetch-data-redux/
  componentWillReceiveProps(nextProps) {
    console.log('nextProps=' + JSON.stringify(nextProps));
    let data = this.state.data;
    let latestData = nextProps.latestData;

    if (latestData != null) {
      let requestId = latestData.requestId;
      let latestOffers = latestData.offers;

      let offersForRequest = [];
      let requestData = data[requestId];
      if (requestData != null) offersForRequest = requestData.offers;

      let offers = [...offersForRequest, ...latestOffers];

      let lastId = -1;
      if (offers.length > 0) lastId = offers[offers.length - 1].id;

      data[requestId] = {
        offers: [...offersForRequest, ...offers],
        lastId: lastId,
        activeItem: -1
      };
    }

    let requests = nextProps.requests;
    let ids = requests
      .map(r => r.id)
      .filter(id => !this.state.requests.includes(id));

    this.setState({
      ...this.state,
      requests: [...this.state.requests, ...ids],
      data: data
    });
  }

  handleMenuClick(event) {
    let id = event.currentTarget.dataset.id;
    this.setState({ ...this.state, activeMenuItem: id });
  }

  handleClick(event) {
    let id = event.currentTarget.dataset.id;
    let requestId = event.currentTarget.dataset.request;

    let data = this.state.data;
    let activeItem = data[requestId].activeItem;

    if (id === activeItem) id = -1;
    data[requestId].activeItem = id;

    this.setState({ ...this.state, data: data });
  }

  handleLoad(event) {
    let requestId = this.state.requests[this.state.activeMenuItem];
    let lastId = -1;

    let requestData = this.state.data[requestId];
    if (requestData != null) {
      lastId = requestData.lastId;
    }

    this.props.fetchOffers(requestId, lastId);
  }

  handleReserve(event) {
    let requestId = this.state.requests[this.state.activeMenuItem];
    let data = this.state.data[requestId];
    let offer = data.offers[data.activeItem];

    this.props.submitReservation(requestId, offer.id);
  }

  createTableEntries(items) {
    let requestId = this.state.requests[this.state.activeMenuItem];

    let entries = [];
    items.forEach((item, i) => {
      let isActive =
        i.toString() === this.state.data[requestId].activeItem.toString();

      entries.push(
        <tr
          className={isActive ? 'info' : ''}
          key={i}
          data-id={i}
          data-request={requestId}
          onClick={this.handleClick}
        >
          <td>{item.id}</td>
          <td>{item.price}</td>
          <td>{item.energy}</td>
          <td>{item.date}</td>
          <td>{item.time.substring(0, 8)}</td>
          <td>{item.window}</td>
        </tr>
      );
    });

    return entries;
  }

  createMenuItems(ids) {
    let items = [];
    ids.forEach((id, i) => {
      let isActive = i.toString() === this.state.activeMenuItem.toString();

      items.push(
        <MenuItem
          key={i}
          data-id={i}
          eventKey={i}
          active={isActive}
          onClick={this.handleMenuClick}
        >
          {'Request ' + id}
        </MenuItem>
      );
    });
    return items;
  }

  // https://stackoverflow.com/questions/50219349/dropdownbutton-menuitem-from-react-bootstrap
  render() {
    let items = [];

    let currentRequest = '';
    let activeMenuItem = this.state.activeMenuItem;
    let hasRequestSelected = activeMenuItem !== -1;
    let hasOfferSelected = false;
    if (hasRequestSelected) {
      let requestId = this.state.requests[activeMenuItem];
      currentRequest = ' | Request ' + requestId;

      let data = this.state.data;
      if (data[requestId] != null) {
        items = data[requestId].offers;
        hasOfferSelected = data[requestId].activeItem !== -1;
      }
    }

    return (
      <div className="OfferView col-sm-6">
        <div className="row">
          <div className="pull-left">
            <h3>{'Offers' + currentRequest}</h3>
          </div>
        </div>

        <div className="row">
          <div className="pull-left">
            <Button
              disabled={!hasRequestSelected}
              bsStyle="primary"
              bsSize="sm"
              type="button"
              onClick={this.handleLoad}
            >
              Load
            </Button>

            <span className="margin-left">
              <DropdownButton
                disabled={this.state.requests.length === 0}
                id="request-dropdown"
                title="Request"
                bsStyle="info"
                bsSize="sm"
                pullRight={false}
              >
                {this.createMenuItems(this.state.requests)}
              </DropdownButton>
            </span>

            <span className="margin-left">
              <Button
                disabled={!hasOfferSelected}
                bsStyle="warning"
                bsSize="sm"
                type="button"
                onClick={this.handleReserve}
              >
                Reserve
              </Button>
            </span>
          </div>
        </div>

        <div className="row">
          <Table responsive striped hover>
            <thead>
              <tr>
                <th className="text-center">ID</th>
                <th className="text-center">Price</th>
                <th className="text-center">Energy</th>
                <th className="text-center">Date</th>
                <th className="text-center">Time</th>
                <th className="text-center">Window</th>
              </tr>
            </thead>
            <tbody>{this.createTableEntries(items)}</tbody>
          </Table>
        </div>
      </div>
    );
  }
}

// https://redux.js.org/api/bindactioncreators
// https://stackoverflow.com/questions/38202572/understanding-react-redux-and-mapstatetoprops

const mapStateToProps = state => ({
  requests: state.offerViewReducer.requests,
  latestData: state.offerViewReducer.latestData
});

const mapDispatchToProps = dispatch => ({
  fetchOffers: (requestId, lastOfferId) =>
    dispatch(fetchOffers(requestId, lastOfferId)),
  submitReservation: (requestId, offerId) =>
    dispatch(submitReservation(requestId, offerId))
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(OfferView);
