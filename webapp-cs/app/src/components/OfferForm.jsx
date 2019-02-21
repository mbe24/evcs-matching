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
import { submitOffer } from '../actions/offerFormActions';

function createDefaultOffer() {
  let date = new Date();
  let now = new Date(date.getTime() - date.getTimezoneOffset() * 60000);

  let offer = {
    id: 0,
    price: 0.0,
    energy: 0,
    date: now.toISOString().substr(0, 10),
    time: now.toISOString().substr(11, 8),
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

  componentWillReceiveProps(nextProps) {
    let offer = nextProps.request;

    if (!nextProps.active) offer = createDefaultOffer();
    else offer.price = Math.round(100 * offer.energy * 0.2) / 100;

    this.setState({
      ...this.state,
      offer: offer,
      active: nextProps.active,
      isLoading: nextProps.isLoading
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

    let requestId = this.state.offer.id;
    this.props.submitOffer(this.state.offer, requestId);
  }

  render() {
    let labelWidth = 2;
    let textfieldWidth = 10;

    return (
      <div className="OfferForm col-sm-6">
        <div className="row">
          <div className="pull-left">
            <h3>Offer</h3>
          </div>
        </div>

        <Form horizontal onSubmit={this.handleSubmit}>
          <FormGroup controlId="id" bsSize="small">
            <Col componentClass={ControlLabel} sm={labelWidth}>
              ID
            </Col>
            <Col sm={textfieldWidth}>
              <FormControl
                disabled
                autoFocus
                type="text"
                value={this.state.offer.id}
              />
            </Col>
          </FormGroup>

          <FormGroup controlId="price" bsSize="small">
            <Col componentClass={ControlLabel} sm={labelWidth}>
              Price
            </Col>
            <Col sm={textfieldWidth}>
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
            <Col componentClass={ControlLabel} sm={labelWidth}>
              Energy
            </Col>
            <Col sm={textfieldWidth}>
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
            <Col componentClass={ControlLabel} sm={labelWidth}>
              Date
            </Col>
            <Col sm={textfieldWidth}>
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
            <Col componentClass={ControlLabel} sm={labelWidth}>
              Time
            </Col>
            <Col sm={textfieldWidth}>
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
            <Col componentClass={ControlLabel} sm={labelWidth}>
              Window
            </Col>
            <Col sm={textfieldWidth}>
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
            <Col smOffset={2} sm={2}>
              <Button
                disabled={!this.state.active || this.state.isLoading}
                bsStyle="primary"
                bsSize="sm"
                type="submit"
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
  request: state.offerFormReducer.request,
  active: state.offerFormReducer.active,
  isLoading: state.offerFormReducer.isLoading
});

const mapDispatchToProps = dispatch => ({
  submitOffer: (offer, requestId) => dispatch(submitOffer(offer, requestId))
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(OfferForm);
