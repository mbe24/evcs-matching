import React from 'react';
import { connect } from 'react-redux';
import './OfferView.css';

// https://react-bootstrap.netlify.com/components/table/

class OfferView extends React.Component {
  render() {
    return (
      <div className="OfferView col-md-4 col-md-offset-4">
        <h3>Offers</h3>
      </div>
    );
  }
}

export default connect(
  null,
  null
)(OfferView);
