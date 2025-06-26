import { Client } from '@stomp/stompjs';

let client = null;

export function connectWebSocket(onReading) {
    if (client && client.active) return client;

    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const host = window.location.host;
    const wsUrl = `${protocol}://${host}/ws-readings/websocket`;

    client = new Client({
        brokerURL: wsUrl,
        reconnectDelay: 5000,
        debug: (str) => console.log(str),
        onConnect: (frame) => {
            console.log('🟢 Conectado al WebSocket:', frame);
            client.subscribe('/topic/readings', (message) => {
                const reading = JSON.parse(message.body);
                console.log('📩 Mensaje recibido:', reading);
                onReading(reading);
            });
        },
        onStompError: (frame) => {
            console.error('Error STOMP:', frame.headers['message']);
            console.error('Detalle:', frame.body);
        },
    });

    client.activate();
    return client;
}
