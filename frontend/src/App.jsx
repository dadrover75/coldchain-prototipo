import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import DeviceChart from './pages/DeviceChart';

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/device/:deviceId" element={<DeviceChart />} />
            </Routes>
        </Router>
    );
}

export default App;
