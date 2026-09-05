import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import PlanningPanel from './PlanningPanel';
import './styles.css';
import './customer.css';
import './planning.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
    <PlanningPanel />
  </React.StrictMode>
);
