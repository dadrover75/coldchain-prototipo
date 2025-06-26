import { useEffect, useState } from 'react';
import { connectWebSocket } from '../websocket';
import DeviceCard from '../components/DeviceCard';

function Dashboard() {
    const [devices, setDevices] = useState({});

    useEffect(() => {
        const client = connectWebSocket((data) => {
            setDevices((prev) => ({
                ...prev,
                [data.device_id]: data,
            }));
        });

        return () => {
            if (client && client.deactivate) client.deactivate();
        };
    }, []);

    return (
        <div>
            <h1>Dispositivos en Tiempo Real</h1>
            <div style={{ display: 'flex', flexWrap: 'wrap' }}>
                {Object.values(devices).map(device => (
                    <DeviceCard key={device.device_id} device={device} />
                ))}
            </div>
        </div>
    );
}

export default Dashboard;
