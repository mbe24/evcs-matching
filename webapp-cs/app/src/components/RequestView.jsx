import React from 'react';
import { connect } from 'react-redux';
import Table from 'react-bootstrap/lib/Table';
import Button from 'react-bootstrap/lib/Button';
import './RequestView.css';
import { setRequestActive, fetchRequests } from '../actions/requestViewActions';

// http://allenfang.github.io/react-bootstrap-table/index.html
// https://react-bootstrap.netlify.com/components/table/
// https://react-bootstrap.github.io/components/modal/

class RequestView extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      lastId: -1,
      activeItem: -1,
      items: []
    };

    this.handleClick = this.handleClick.bind(this);
    this.handleLoad = this.handleLoad.bind(this);
    this.createTableEntries = this.createTableEntries.bind(this);
  }

  // https://daveceddia.com/where-fetch-data-redux/
  componentWillReceiveProps(nextProps) {
    let requests = nextProps.requests;
    if (requests == null || requests.length === 0) return;

    let lastId = requests[requests.length - 1].id;

    requests.forEach((request, i) => {
      let isActive = i.toString() === this.state.activeItem.toString();
      request.isActive = isActive;
      let index = request.time.lastIndexOf('.');
      if (index !== -1) request.time = request.time.substr(0, index);
    });

    this.setState({
      ...this.state,
      items: [...this.state.items, ...requests],
      lastId: lastId
    });
  }

  handleClick(event) {
    let id = event.currentTarget.dataset.id;
    if (id === this.state.activeItem) id = -1;
    this.setState({ ...this.state, activeItem: id });

    let hasRequest = id !== -1;
    let request = hasRequest ? this.state.items[id] : {};
    this.props.setRequestActive(request, hasRequest);
  }

  handleLoad(event) {
    this.props.fetchRequests(this.state.lastId);
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
          <td>{item.id}</td>
          <td>{item.energy}</td>
          <td>{item.date}</td>
          <td>{item.time}</td>
          <td>{item.window}</td>
        </tr>
      );
    });

    return entries;
  }

  render() {
    return (
      <div className="RequestView col-sm-6">
        <div className="row">
          <div className="pull-left">
            <h3>Requests</h3>
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
          </div>
        </div>

        <div className="row">
          <Table responsive striped hover>
            <thead>
              <tr>
                <th className="text-center">ID</th>
                <th className="text-center">Energy</th>
                <th className="text-center">Date</th>
                <th className="text-center">Time</th>
                <th className="text-center">Window</th>
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
  requests: state.requestViewReducer.requests
});

const mapDispatchToProps = dispatch => ({
  setRequestActive: (request, active) =>
    dispatch(setRequestActive(request, active)),
  fetchRequests: lastRequestId => dispatch(fetchRequests(lastRequestId))
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(RequestView);
