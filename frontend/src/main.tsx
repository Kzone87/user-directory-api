import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import PlanningPanel from './PlanningPanel';
import AnalyticsPanel from './AnalyticsPanel';
import ApprovalReportingPanel from './ApprovalReportingPanel';
import './styles.css';
import './customer.css';
import './planning.css';
import './analytics.css';
import './approval-reporting.css';
import './product-ui.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
    <PlanningPanel />
    <AnalyticsPanel />
    <ApprovalReportingPanel />
  </React.StrictMode>
);
