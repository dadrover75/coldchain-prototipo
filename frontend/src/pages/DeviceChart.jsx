import { useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';

function DeviceChart() {
    const { deviceId } = useParams();
    const [data, setData] = useState([]);
    const [ledgerHistory, setLedgerHistory] = useState('');
    const [contractState, setContractState] = useState('');
    const [loadingHistory, setLoadingHistory] = useState(false);
    const [loadingState, setLoadingState] = useState(false);
    const [loadingReset, setLoadingReset] = useState(false); // nuevo estado
    const [errorHistory, setErrorHistory] = useState(null);
    const [errorState, setErrorState] = useState(null);
    const [errorReset, setErrorReset] = useState(null); // error reset

    const fetchData = async () => {
        try {
            const res = await fetch(`/api/readings/device/${deviceId}`);
            if (!res.ok) throw new Error('Error obteniendo lecturas DB');
            const json = await res.json();
            setData(json);
        } catch (e) {
            console.error(e);
        }
    };

    const fetchLedgerHistory = async () => {
        setLoadingHistory(true);
        setErrorHistory(null);
        try {
            const res = await fetch(`/ledger/lectura/${deviceId}/historial`);
            if (!res.ok) throw new Error(`Error status ${res.status}`);
            const text = await res.text();
            setLedgerHistory(text);
        } catch (e) {
            setErrorHistory(e.message);
            setLedgerHistory('');
        } finally {
            setLoadingHistory(false);
        }
    };

    const fetchContractState = async () => {
        setLoadingState(true);
        setErrorState(null);
        try {
            const res = await fetch(`/ledger/estado`);
            if (!res.ok) throw new Error(`Error status ${res.status}`);
            const text = await res.text();
            setContractState(text);
        } catch (e) {
            setErrorState(e.message);
            setContractState('');
        } finally {
            setLoadingState(false);
        }
    };

    // Nuevo: resetear estado del contrato
    const resetContractState = async () => {
        setLoadingReset(true);
        setErrorReset(null);
        try {
            const res = await fetch(`/ledger/reset`, { method: 'POST' });
            if (!res.ok) throw new Error(`Error status ${res.status}`);
            const text = await res.text();
            // Mostrar respuesta del reset en el campo y luego refrescar estado
            setContractState(text);
            await fetchContractState();
        } catch (e) {
            setErrorReset(e.message);
        } finally {
            setLoadingReset(false);
        }
    };

    useEffect(() => {
        fetchData();
        // Cargar estado contrato automáticamente al entrar
        fetchContractState();
        const interval = setInterval(fetchData, 10000); // cada 10 segundos
        return () => clearInterval(interval);
    }, [deviceId]);

    // Limitar a últimas 10 lecturas ordenadas cronológicamente
    const chartData = [...data]
        .sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp))
        .slice(-10);

    // Color para estado contrato
    const contractColor = contractState.includes('fail') ? '#ffdddd' : contractState.includes('success') ? '#ddffdd' : '#ffffff';

    return (
        <div style={{ padding: '20px' }}>
            <h2>Histórico del dispositivo {deviceId}</h2>
            <LineChart width={800} height={400} data={chartData}>
                <CartesianGrid stroke="#ccc" />
                <XAxis dataKey="timestamp" tickFormatter={ts => new Date(ts).toLocaleTimeString()} />
                <YAxis />
                <Tooltip />
                <Line type="monotone" dataKey="temperature" stroke="#8884d8" />
            </LineChart>

            <div style={{ marginTop: '30px', display: 'flex', gap: '40px', flexWrap: 'wrap' }}>
                {/* Historial Ledger */}
                <div style={{ flex: '1 1 400px', maxWidth: '600px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '10px' }}>
                        <button onClick={fetchLedgerHistory} disabled={loadingHistory} style={{ padding: '8px 14px' }}>
                            {loadingHistory ? 'Cargando...' : 'Hist'}
                        </button>
                        <span style={{ fontSize: '0.9em', color: '#555' }}>Historial raw (ledger)</span>
                    </div>
                    <textarea
                        readOnly
                        value={errorHistory ? `ERROR: ${errorHistory}` : ledgerHistory}
                        placeholder="Pulsa Hist para cargar historial desde ledger"
                        style={{ width: '100%', height: '200px', fontFamily: 'monospace', fontSize: '12px', padding: '10px' }}
                    />
                </div>

                {/* Estado Contrato + Reset */}
                <div style={{ flex: '1 1 300px', maxWidth: '420px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '10px', flexWrap: 'wrap' }}>
                        <button onClick={fetchContractState} disabled={loadingState} style={{ padding: '8px 14px' }}>
                            {loadingState ? 'Cargando...' : 'Contrato'}
                        </button>
                        <button onClick={resetContractState} disabled={loadingReset} style={{ padding: '8px 14px' }}>
                            {loadingReset ? 'Reseteando...' : 'Reset'}
                        </button>
                        <span style={{ fontSize: '0.9em', color: '#555' }}>Estado y reset del contrato</span>
                    </div>
                    <input
                        readOnly
                        value={errorState ? `ERROR: ${errorState}` : (errorReset ? `RESET ERROR: ${errorReset}` : contractState)}
                        placeholder="Pulsa Contrato o Reset"
                        style={{ width: '100%', padding: '10px', fontFamily: 'monospace', fontSize: '14px', backgroundColor: contractColor, border: '1px solid #ccc', borderRadius: '4px' }}
                    />
                </div>
            </div>
        </div>
    );
}

export default DeviceChart;
