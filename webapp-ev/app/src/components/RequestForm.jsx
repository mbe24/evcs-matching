import React from 'react';
import { connect } from 'react-redux';
import Button from 'react-bootstrap/lib/Button';
import Form from 'react-bootstrap/lib/Form';
import FormGroup from 'react-bootstrap/lib/FormGroup';
import InputGroup from 'react-bootstrap/lib/InputGroup';
import FormControl from 'react-bootstrap/lib/FormControl';
import ControlLabel from 'react-bootstrap/lib/ControlLabel';
import Col from 'react-bootstrap/lib/Col';
import './RequestForm.css';
import { submitRequest } from '../actions/requestFormActions';

function createDefaultRequest() {
  let date = new Date();
  let now = new Date(date.getTime() - date.getTimezoneOffset() * 60000);

  let request = {
    energy: 10 + Math.floor(100 * Math.random() * 90) / 100,
    date: now.toISOString().substr(0, 10),
    time: now.toISOString().substr(11, 8),
    window: 1 + Math.floor(7.5 * Math.random())
  };

  return request;
}

class RequestForm extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      isLoading: false,
      request: createDefaultRequest()
    };

    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    this.setState({
      ...this.state,
      isLoading: nextProps.isLoading
    });
  }

  handleChange(event) {
    this.setState({
      ...this.state,
      request: { ...this.state.request, [event.target.id]: event.target.value }
    });
  }

  handleSubmit(event) {
    event.preventDefault();
    this.props.submitRequest(this.state.request);

    let date = new Date();
    let now = new Date(date.getTime() - date.getTimezoneOffset() * 60000);

    let request = this.state.request;
    request.date = now.toISOString().substr(0, 10);
    request.time = now.toISOString().substr(11, 8);

    this.setState({
      ...this.state,
      request: request
    });
  }

  render() {
    let labelWidth = 2;
    let textfieldWidth = 10;

    return (
      <div className="RequestForm col-sm-6">
        <div className="row">
          <div className="pull-left">
            <h3>Request</h3>
          </div>
        </div>

        <Form horizontal onSubmit={this.handleSubmit}>
          <FormGroup controlId="energy" bsSize="small">
            <Col componentClass={ControlLabel} sm={labelWidth}>
              Energy
            </Col>
            <Col sm={textfieldWidth}>
              <InputGroup>
                <FormControl
                  autoFocus
                  type="number"
                  step="0.01"
                  value={this.state.request.energy}
                  onChange={this.handleChange}
                />
                <InputGroup.Addon>
                  <b>kWh</b>
                </InputGroup.Addon>
              </InputGroup>
            </Col>
          </FormGroup>

          <FormGroup controlId="date" bsSize="small">
            <Col componentClass={ControlLabel} sm={labelWidth}>
              Date
            </Col>
            <Col sm={textfieldWidth}>
              <FormControl
                autoFocus
                type="date"
                value={this.state.request.date}
                onChange={this.handleChange}
              />
            </Col>
          </FormGroup>

          <FormGroup controlId="time" bsSize="small">
            <Col componentClass={ControlLabel} sm={labelWidth}>
              Time
            </Col>
            <Col sm={textfieldWidth}>
              <FormControl
                autoFocus
                type="time"
                value={this.state.request.time}
                onChange={this.handleChange}
              />
            </Col>
          </FormGroup>

          <FormGroup controlId="window" bsSize="small">
            <Col componentClass={ControlLabel} sm={labelWidth}>
              Window
            </Col>
            <Col sm={textfieldWidth}>
              <InputGroup>
                <FormControl
                  autoFocus
                  type="number"
                  step="1"
                  value={this.state.request.window}
                  onChange={this.handleChange}
                />
                <InputGroup.Addon>
                  <b>h</b>
                </InputGroup.Addon>
              </InputGroup>
            </Col>
          </FormGroup>

          <FormGroup>
            <Col smOffset={2} sm={2}>
              <Button
                bsStyle="primary"
                bsSize="sm"
                type="submit"
                disabled={this.state.isLoading}
                onClick={!this.state.isLoading ? this.handleSubmit : null}
              >
                Submit
              </Button>
            </Col>
          </FormGroup>
        </Form>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  isLoading: state.requestFormReducer.isLoading
});

const mapDispatchToProps = dispatch => ({
  submitRequest: request => dispatch(submitRequest(request))
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(RequestForm);
