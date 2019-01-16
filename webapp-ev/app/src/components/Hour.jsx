import React from 'react';
import { connect } from 'react-redux';
import './Hour.css';

class Hour extends React.Component {
  render() {
    return <p className="Hour">{this.props.hour}:</p>;
  }
}

const mapStateToProps = state => ({ hour: state.hourReducer.hour });

export default connect(
  mapStateToProps,
  null
)(Hour);
