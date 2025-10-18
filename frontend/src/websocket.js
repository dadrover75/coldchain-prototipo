import { Client } from '@stomp/stompjs';

let client = null;
let subscription = null;
let failingPrimary = false;

function buildUrls() {
  const host = window.location.hostname;
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
  return {
    primary: `${protocol}://${host}/ws-readings`,      // vía Nginx (puerto 80)
    fallback: `${protocol}://${host}:8080/ws-readings` // directo backend
  };
}

export function connectWebSocket(onReading, onStatus) {
  if (client && client.active) return client;
  const { primary, fallback } = buildUrls();
  const target = failingPrimary ? fallback : primary;
  if (onStatus) onStatus('connecting:' + (failingPrimary ? 'fallback' : 'primary'));

  client = new Client({
    brokerURL: target,
    reconnectDelay: 5000, // reintentos automáticos solo cuando ya conectado alguna vez
    debug: (str) => console.log('[STOMP]', str),
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect: frame => {
      console.log('🟢 STOMP conectado a', target, 'cmd:', frame.command);
      if (onStatus) onStatus('connected:' + (failingPrimary ? 'fallback' : 'primary'));
      if (subscription) { try { subscription.unsubscribe(); } catch (_) {} }
      subscription = client.subscribe('/topic/readings', msg => {
        try { onReading(JSON.parse(msg.body)); } catch (e) { console.error('Parse lectura WS', e, msg.body); }
      });
    },
    onStompError: frame => {
      console.error('❌ STOMP error', frame.headers['message']);
      if (onStatus) onStatus('stomp-error:' + frame.headers['message']);
    },
    onWebSocketClose: evt => {
      console.warn('🔌 Cierre WS', target, 'code:', evt.code, 'reason:', evt.reason);
      if (!failingPrimary) {
        // Intentar fallback sólo una vez si primary nunca conectó
        console.log('[WS] Activando fallback a backend:8080');
        failingPrimary = true;
        try { client.deactivate(); } catch (_) {}
        client = null;
        setTimeout(() => connectWebSocket(onReading, onStatus), 500);
      } else {
        if (onStatus) onStatus('closed');
      }
    },
    onWebSocketError: evt => {
      console.error('❌ WS error en', target, evt);
    }
  });

  try { client.activate(); } catch (e) {
    console.error('Fallo activar STOMP', e);
    if (!failingPrimary) {
      failingPrimary = true;
      setTimeout(() => connectWebSocket(onReading, onStatus), 300);
    } else if (onStatus) onStatus('activate-failed');
  }
  return client;
}

export function disconnectWebSocket() {
  if (subscription) { try { subscription.unsubscribe(); } catch (_) {} subscription = null; }
  if (client) { const c = client; client = null; try { c.deactivate(); } catch (e) { console.error('Error desactivando STOMP', e); } }
  failingPrimary = false;
}
