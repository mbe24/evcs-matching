import React from 'react';
import { connect } from 'react-redux';
import Table from 'react-bootstrap/lib/Table';
import './ReservationView.css';
import { setRequestActive } from '../actions/requestViewActions';

// http://allenfang.github.io/react-bootstrap-table/index.html
// https://react-bootstrap.netlify.com/components/table/
// https://react-bootstrap.github.io/components/modal/

function randomDateInRange(start, days) {
  return new Date(start.getTime() + Math.random() * days * 24 * 60 * 60 * 1000);
}

function* range(start, end) {
  for (let i = start; i < end; i++) {
    yield i;
  }
}

function createItems(data) {
  let date = new Date();
  let now = new Date(date.getTime() - date.getTimezoneOffset() * 60000);

  let items = [];
  data.forEach(i => {
    let random = randomDateInRange(now, 7);
    let date = random.toISOString().substr(0, 10);
    let time = random.toISOString().substr(11, 5);
    let isActive = false;
    let energy =
      20 +
      Math.ceil(80 * Math.random()) +
      (25 * Math.ceil(4 * Math.random())) / 100;
    let window = 15 + 15 * Math.ceil((240 / 15) * Math.random());

    items.push({
      id: i,
      isActive: isActive,
      date: date,
      time: time,
      energy: energy,
      window: window
    });
  });

  return items;
}

class ReservationView extends React.Component {
  constructor(props) {
    super(props);

    let n = 50;
    let elements = [...range(0, n)];
    this.state = {
      activeItem: -1,
      items: createItems(elements)
    };

    this.handleClick = this.handleClick.bind(this);
    this.createTableEntries = this.createTableEntries.bind(this);
  }

  handleClick(event) {
    let id = event.currentTarget.dataset.id;
    if (id === this.state.activeItem) id = -1;
    this.setState({ ...this.state, activeItem: id });

    //let hasRequest = id !== -1;
    //let request = hasRequest ? this.state.items[id] : {};
    //this.props.setRequestActive(request, hasRequest);
  }

  createTableEntries(items) {
    let entries = [];
    items.forEach(item => {
      let isActive = item.id.toString() === this.state.activeItem.toString();

      entries.push(
        <tr
          className={isActive ? 'info' : ''}
          key={item.id}
          data-id={item.id}
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
      <div className="TableView col-sm-6">
        <h3>Reservations</h3>
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
    );
  }
}

// https://redux.js.org/api/bindactioncreators
// https://stackoverflow.com/questions/38202572/understanding-react-redux-and-mapstatetoprops

const mapDispatchToProps = dispatch => ({
  setRequestActive: (request, active) =>
    dispatch(setRequestActive(request, active))
});

export default connect(
  null,
  mapDispatchToProps
)(ReservationView);
