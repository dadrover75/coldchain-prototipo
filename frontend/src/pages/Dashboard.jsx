import { useEffect, useState } from 'react';
import { connectWebSocket, disconnectWebSocket } from '../websocket';
import DeviceCard from '../components/DeviceCard';

function Dashboard() {
    const [devices, setDevices] = useState({});
    const [wsStatus, setWsStatus] = useState('idle');

    useEffect(() => {
        const client = connectWebSocket((data) => {
            setDevices(prev => ({ ...prev, [data.device_id]: data }));
        }, status => setWsStatus(status));
        return () => { disconnectWebSocket(); if (client && client.deactivate) { try { client.deactivate(); } catch (_) {} } };
    }, []);

    return (
        <div>
            <h1>Dispositivos en Tiempo Real</h1>
            <p style={{ fontSize: '0.85em', color: '#555' }}>Estado WS: {wsStatus}</p>
            <div style={{ display: 'flex', flexWrap: 'wrap' }}>
                {Object.values(devices).map(device => (
                    <DeviceCard key={device.device_id} device={device} />
                ))}
            </div>
        </div>
    );
}

export default Dashboard;
