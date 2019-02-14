import React from 'react';
import { connect } from 'react-redux';
import ListGroup from 'react-bootstrap/lib/ListGroup';
import ListGroupItem from 'react-bootstrap/lib/ListGroupItem';
import './OfferView.css';

// https://react-bootstrap.netlify.com/components/table/
// https://react-bootstrap.github.io/components/modal/

function* range(start, end) {
  for (let i = start; i < end; i++) {
    yield i;
  }
}

class OfferView extends React.Component {
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
        <ListGroupItem
          active={isActive}
          key={i}
          data-id={i}
          onClick={this.handleClick}
        >
          Item {i}
        </ListGroupItem>
      );
    });
    return items;
  }

  render() {
    return (
      <div className="OfferView col-sm-6">
        <h3>Offers</h3>
        <ListGroup bsClass="list-group">
          {this.createItems(this.state.data)}
        </ListGroup>
      </div>
    );
  }
}

export default connect(
  null,
  null
)(OfferView);
