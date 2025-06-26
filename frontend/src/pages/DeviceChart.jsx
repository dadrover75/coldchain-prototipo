import { useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';

function DeviceChart() {
    const { deviceId } = useParams();
    const [data, setData] = useState([]);

    const fetchData = async () => {
        const res = await fetch(`/api/readings/device/${deviceId}`);
        const json = await res.json();
        setData(json);
    };

    useEffect(() => {
        fetchData();
        const interval = setInterval(fetchData, 10000); // cada 10 segundos
        return () => clearInterval(interval);
    }, []);

    return (
        <div>
            <h2>Histórico del dispositivo {deviceId}</h2>
            <LineChart width={800} height={400} data={data.reverse()}>
                <CartesianGrid stroke="#ccc" />
                <XAxis dataKey="timestamp" tickFormatter={ts => new Date(ts).toLocaleTimeString()} />
                <YAxis />
                <Tooltip />
                <Line type="monotone" dataKey="temperature" stroke="#8884d8" />
            </LineChart>
        </div>
    );
}

export default DeviceChart;
