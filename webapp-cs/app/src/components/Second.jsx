import React from 'react';
import { connect } from 'react-redux';
import './Second.css';

class Second extends React.Component {
  render() {
    return <p className="Second">{this.props.second}</p>;
  }
}

const mapStateToProps = state => ({ second: state.secondReducer.second });

export default connect(
  mapStateToProps,
  null
)(Second);
