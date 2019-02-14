import React from 'react';
import { connect } from 'react-redux';
import Table from 'react-bootstrap/lib/Table';
import './TableView.css';

// http://allenfang.github.io/react-bootstrap-table/index.html
// https://react-bootstrap.netlify.com/components/table/
// https://react-bootstrap.github.io/components/modal/

function* range(start, end) {
  for (let i = start; i < end; i++) {
    yield i;
  }
}

class TableView extends React.Component {
  constructor(props) {
    super(props);

    let n = 50;
    this.state = { data: [...range(0, n)], activeItem: -1 };

    this.handleClick = this.handleClick.bind(this);
    this.createItems = this.createItems.bind(this);
  }

  handleClick(event) {
    let id = event.currentTarget.dataset.id;
    if (id === this.state.activeItem) id = -1;
    this.setState({ ...this.state, activeItem: id });
  }

  createItems(data) {
    let items = [];
    data.forEach(i => {
      let isActive = i.toString() === this.state.activeItem.toString();
      items.push(
        <tr
          class={isActive ? 'info' : ''}
          key={i}
          data-id={i}
          onClick={this.handleClick}
        >
          <td>{i}</td>
          <td>Item {i}</td>
          <td>{isActive ? 'true' : 'false'}</td>
        </tr>
      );
    });
    return items;
  }

  render() {
    return (
      <div className="TableView col-sm-6">
        <h3>Offers</h3>
        <Table responsive striped hover>
          <thead>
            <tr>
              <th class="text-center">#</th>
              <th class="text-center">Name</th>
              <th class="text-center">Active</th>
            </tr>
          </thead>
          <tbody>{this.createItems(this.state.data)}</tbody>
        </Table>
      </div>
    );
  }
}

export default connect(
  null,
  null
)(TableView);
