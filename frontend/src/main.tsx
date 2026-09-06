import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import PlanningPanel from './PlanningPanel';
import AnalyticsPanel from './AnalyticsPanel';
import './styles.css';
import './customer.css';
import './planning.css';
import './analytics.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
    <PlanningPanel />
    <AnalyticsPanel />
  </React.StrictMode>
);
