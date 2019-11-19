import React, { Component } from 'react';
import './Debts.css';
import { getCurrentUserDebts } from '../../util/APIUtils';

class Debts extends Component {
    constructor(props) {
        super(props);
        console.log(props);
        const debtItem = props.userDebts.map((debt) => 
            <li>{debt.id}</li>
        );
        this.state = {
            debts: debtItem
        }
    }

    render() {
        return (
            <div className="debts-container">
                <div className="container">
                    <div className="debts-header">
                        <h1>Your Current Debts</h1>
                        <ul>{this.state.debts}</ul>
                        
                    </div>
                </div>    
            </div>
        );
    }
}

export default Debts