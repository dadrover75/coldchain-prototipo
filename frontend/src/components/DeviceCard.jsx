import { useNavigate } from 'react-router-dom';

function DeviceCard({ device }) {
    const navigate = useNavigate();

    const color = device.temperature > 10 || device.temperature < -5
        ? 'red' : device.temperature < -2 || device.temperature > 7
            ? 'orange' : 'green';

    return (
        <div onClick={() => navigate(`/device/${device.device_id}`)}
             style={{
                 margin: '10px', padding: '15px',
                 border: '2px solid', borderColor: color,
                 borderRadius: '8px', cursor: 'pointer'
             }}>
            <h3>Dispositivo {device.device_id}</h3>
            <p>Temperatura: {device.temperature.toFixed(2)}°C</p>
            <p>Hora: {new Date(device.timestamp).toLocaleTimeString()}</p>
        </div>
    );
}

export default DeviceCard;
