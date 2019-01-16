import React from 'react';
import { connect } from 'react-redux';
import Button from 'react-bootstrap/lib/Button';
import Form from 'react-bootstrap/lib/Form';
import FormGroup from 'react-bootstrap/lib/FormGroup';
import FormControl from 'react-bootstrap/lib/FormControl';
import ControlLabel from 'react-bootstrap/lib/ControlLabel';
import Col from 'react-bootstrap/lib/Col';
import './RequestForm.css';

function createDefaultRequest() {
  let now = new Date();
  return {
    energy: 99.9,
    date: now.toISOString().substr(0, 10),
    time: now.getHours() + ':' + now.getMinutes(),
    duration: 2
  };
}

class RequestForm extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      isLoading: false,
      ...createDefaultRequest(99.9)
    };

    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleChange(event) {
    this.setState({ ...this.state, [event.target.id]: event.target.value });
  }

  handleSubmit(event) {
    event.preventDefault();
    this.setState({ ...this.state, isLoading: true });

    const request = {
      energy: this.state.energy,
      date: this.state.date,
      time: this.state.time,
      duration: this.state.duration
    };

    fetch('/app/api/v1/request/create', {
      method: 'post',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(request)
    })
      .then(_ => this.setState({ ...this.state, isLoading: false }))
      //.then(response => response.json())
      //.then(data => console.log(data))
      .catch(err => {
        console.log('Error while posting request...');
        console.log(err);
      });

    this.setState({
      ...this.state,
      ...{ ...createDefaultRequest(0), energy: 0, duration: 0 }
    });
  }

  render() {
    return (
      <div className="RequestForm col-sm-4 col-sm-offset-4">
        <h3>Request</h3>
        <Form horizontal onSubmit={this.handleSubmit}>
          <FormGroup controlId="energy" bsSize="small">
            <Col componentClass={ControlLabel} sm={3}>
              Energy
            </Col>
            <Col sm={9}>
              <FormControl
                autoFocus
                type="number"
                step="0.01"
                value={this.state.energy}
                onChange={this.handleChange}
              />
            </Col>
          </FormGroup>

          <FormGroup controlId="date" bsSize="small">
            <Col componentClass={ControlLabel} sm={3}>
              Date
            </Col>
            <Col sm={9}>
              <FormControl
                autoFocus
                type="date"
                value={this.state.date}
                onChange={this.handleChange}
              />
            </Col>
          </FormGroup>

          <FormGroup controlId="time" bsSize="small">
            <Col componentClass={ControlLabel} sm={3}>
              Time
            </Col>
            <Col sm={9}>
              <FormControl
                autoFocus
                type="time"
                value={this.state.time}
                onChange={this.handleChange}
              />
            </Col>
          </FormGroup>

          <FormGroup controlId="duration" bsSize="small">
            <Col componentClass={ControlLabel} sm={3}>
              Duration
            </Col>
            <Col sm={9}>
              <FormControl
                autoFocus
                type="number"
                step="1"
                value={this.state.duration}
                onChange={this.handleChange}
              />
            </Col>
          </FormGroup>

          <FormGroup>
            <Col smOffset={3} sm={2}>
              <Button
                bsStyle="primary"
                bsSize="sm"
                type="submit"
                disabled={this.state.isLoading}
                onClick={!this.state.isLoading ? this.handleClick : null}
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

export default connect(
  null,
  null
)(RequestForm);
