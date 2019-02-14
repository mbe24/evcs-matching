import React from 'react';
import { connect } from 'react-redux';
import Button from 'react-bootstrap/lib/Button';
import Form from 'react-bootstrap/lib/Form';
import FormGroup from 'react-bootstrap/lib/FormGroup';
import InputGroup from 'react-bootstrap/lib/InputGroup';
import FormControl from 'react-bootstrap/lib/FormControl';
import ControlLabel from 'react-bootstrap/lib/ControlLabel';
import Col from 'react-bootstrap/lib/Col';
import './OfferForm.css';

function createDefaultOffer() {
  let date = new Date();
  let now = new Date(date.getTime() - date.getTimezoneOffset() * 60000);

  let offer = {
    id: 0,
    price: 0.0,
    energy: 0,
    date: now.toISOString().substr(0, 10),
    time: now.toISOString().substr(11, 5),
    window: 0
  };
  return offer;
}

class OfferForm extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      isLoading: false,
      offer: createDefaultOffer(),
      active: false
    };

    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  // TODO read price from server response
  componentWillReceiveProps(nextProps) {
    let offer = nextProps.request;
    if (!nextProps.active) offer = createDefaultOffer();
    else offer.price = Math.round(100 * offer.energy * 0.2) / 100;

    this.setState({
      ...this.state,
      offer: offer,
      active: nextProps.active
    });
  }

  handleChange(event) {
    this.setState({
      ...this.state,
      offer: { ...this.state.offer, [event.target.id]: event.target.value }
    });
  }

  handleSubmit(event) {
    event.preventDefault();
    this.setState({ ...this.state, isLoading: true });

    let request = Object.assign({}, this.state.offer);

    // TODO move to actions, evaluate answer
    fetch('/app/api/v1/offer/create', {
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
  }

  render() {
    return (
      <div className="OfferForm col-sm-6">
        <h3>Offer</h3>
        <Form horizontal onSubmit={this.handleSubmit}>
          <FormGroup controlId="id" bsSize="small">
            <Col componentClass={ControlLabel} sm={3}>
              ID
            </Col>
            <Col sm={9}>
              <FormControl
                disabled
                autoFocus
                type="text"
                value={this.state.offer.id}
              />
            </Col>
          </FormGroup>

          <FormGroup controlId="price" bsSize="small">
            <Col componentClass={ControlLabel} sm={3}>
              Price
            </Col>
            <Col sm={9}>
              <InputGroup>
                <FormControl
                  disabled={!this.state.active}
                  autoFocus
                  type="number"
                  step="0.01"
                  value={this.state.offer.price}
                  onChange={this.handleChange}
                />
                <InputGroup.Addon>
                  <b>&euro;</b>
                </InputGroup.Addon>
              </InputGroup>
            </Col>
          </FormGroup>

          <FormGroup controlId="energy" bsSize="small">
            <Col componentClass={ControlLabel} sm={3}>
              Energy
            </Col>
            <Col sm={9}>
              <InputGroup>
                <FormControl
                  disabled={true}
                  autoFocus
                  type="number"
                  step="0.01"
                  value={this.state.offer.energy}
                />
                <InputGroup.Addon>
                  <b>kWh</b>
                </InputGroup.Addon>
              </InputGroup>
            </Col>
          </FormGroup>

          <FormGroup controlId="date" bsSize="small">
            <Col componentClass={ControlLabel} sm={3}>
              Date
            </Col>
            <Col sm={9}>
              <FormControl
                disabled={!this.state.active}
                autoFocus
                type="date"
                value={this.state.offer.date}
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
                disabled={!this.state.active}
                autoFocus
                type="time"
                value={this.state.offer.time}
                onChange={this.handleChange}
              />
            </Col>
          </FormGroup>

          <FormGroup controlId="window" bsSize="small">
            <Col componentClass={ControlLabel} sm={3}>
              Window
            </Col>
            <Col sm={9}>
              <InputGroup>
                <FormControl
                  disabled={!this.state.active}
                  autoFocus
                  type="number"
                  step="1"
                  placeholder={this.state.offer.window}
                  onChange={this.handleChange}
                />
                <InputGroup.Addon>
                  <b>min</b>
                </InputGroup.Addon>
              </InputGroup>
            </Col>
          </FormGroup>

          <FormGroup>
            <Col smOffset={3} sm={2}>
              <Button
                disabled={!this.state.active || this.state.isLoading}
                bsStyle="primary"
                bsSize="sm"
                type="submit"
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

const mapStateToProps = state => ({
  request: state.offerFormReducer.request,
  active: state.offerFormReducer.active
});

export default connect(
  mapStateToProps,
  null
)(OfferForm);
