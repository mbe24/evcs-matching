import React from 'react';
import { connect } from 'react-redux';
import './Minute.css';

class Minute extends React.Component {
  render() {
    return <p className="Minute">{this.props.minute}:</p>;
  }
}

const mapStateToProps = state => ({ minute: state.minuteReducer.minute });

export default connect(
  mapStateToProps,
  null
)(Minute);
