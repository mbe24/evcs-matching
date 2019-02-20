import React from 'react';
import { connect } from 'react-redux';
import Button from 'react-bootstrap/lib/Button';
import DropdownButton from 'react-bootstrap/lib/DropdownButton';
import MenuItem from 'react-bootstrap/lib/MenuItem';
import Table from 'react-bootstrap/lib/Table';
import './OfferView.css';
import { fetchOffers } from '../actions/offerViewActions';

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
    this.handleLoad = this.handleLoad.bind(this);
    this.createTableEntries = this.createTableEntries.bind(this);
  }

  // https://daveceddia.com/where-fetch-data-redux/
  componentWillReceiveProps(nextProps) {
    let data = this.state.data;
    let latestData = nextProps.latestData;
    if (latestData != null) {
      let requestId = latestData.requestId;
      let offers = latestData.offers;
      let lastId = offers[offers.length - 1].id;

      let offersForRequest = [];
      let requestData = data[requestId];
      if (requestData != null) offersForRequest = requestData.offers;

      data[requestId] = {
        offers: [...offersForRequest, ...offers],
        lastId: lastId
      };
    }

    let requests = nextProps.requests;
    let ids = requests.map(r => r.id);

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

  handleLoad(event) {
    let requestId = this.state.requests[this.state.activeMenuItem];
    let lastId = -1;

    let requestData = this.state.data[requestId];
    if (requestData != null) {
      lastId = requestData.lastId;
    }

    this.props.fetchOffers(requestId, lastId);
  }

  createTableEntries(items) {
    let entries = [];
    items.forEach(item => {
      entries.push(
        <tr key={item.id} data-id={item.id}>
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
    if (hasRequestSelected) {
      let requestId = this.state.requests[activeMenuItem];
      currentRequest = ' | Request ' + requestId;

      let data = this.state.data;
      if (data[requestId] != null) items = data[requestId].offers;
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
    dispatch(fetchOffers(requestId, lastOfferId))
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(OfferView);
