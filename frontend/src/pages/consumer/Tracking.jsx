import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Package, Truck, CheckCircle, Clock } from 'lucide-react';

/**
 * Tracking Component (Consumer Dashboard)
 * 
 * Displays the real-time tracking timeline for a specific order.
 * Uses Server-Sent Events (SSE) to listen for targeted status updates pushed
 * by the backend orchestrator whenever the assigned rider changes the status.
 * 
 * @author Ankit Sinha
 */
export default function Tracking() {
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    // 1. Fetch initial state of the order so we can draw the timeline immediately
    axios.get(`http://localhost:8080/api/orders/${id}`).then(res => setOrder(res.data));

    // 2. Establish a dedicated, real-time SSE connection just for this specific order
    const token = localStorage.getItem('token');
    const sse = new EventSource(`http://localhost:8080/api/orders/${id}/stream?token=${token}`);
    
    // 3. Listen for specific targeted updates from the Orchestrator
    sse.addEventListener("status-update", (e) => {
      // Whenever the rider hits 'Update Status', this fires instantly.
      setOrder(prev => prev ? { ...prev, status: e.data } : null);
    });

    // Cleanup connection when the user leaves the tracking page
    return () => sse.close();
  }, [id]);

  /**
   * Action triggered if the consumer disputes the 'DELIVERED' status.
   * This immediately routes the order into a SUPPORT_ROUTED state.
   */
  const reportNotReceived = async () => {
    await axios.post(`http://localhost:8080/api/orders/${id}/not-received`);
    navigate('/consumer/support');
  };

  if (!order) return <div className="text-center mt-10">Loading tracking...</div>;

  // Define the linear timeline of states
  const states = ['ORDER_RECEIVED', 'PARTNER_ACCEPTED', 'EN_ROUTE_TO_PICKUP', 'EN_ROUTE_TO_DELIVERY', 'DELIVERED'];
  const currentIndex = states.indexOf(order.status);
  
  // If the order has been disputed, show the support screen instead of the timeline
  if (order.status === 'SUPPORT_ROUTED') {
    return (
      <div className="glass p-8 text-center max-w-lg mx-auto">
        <h2 className="text-3xl font-bold text-red-400 mb-4">Support Ticket Opened</h2>
        <p>We are looking into why your order was not received. A representative will contact you shortly.</p>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto glass p-8">
      <h2 className="text-3xl font-bold mb-8 text-center">Tracking Order #{order.id}</h2>
      
      {/* Visual Timeline UI */}
      <div className="relative mb-12">
        {/* Background Grey Line */}
        <div className="absolute top-1/2 left-0 w-full h-1 bg-gray-700 -translate-y-1/2 z-0"></div>
        {/* Active Pink Progress Line */}
        <div className="absolute top-1/2 left-0 h-1 bg-primary -translate-y-1/2 z-0 transition-all duration-500" style={{ width: `${(currentIndex / (states.length - 1)) * 100}%` }}></div>
        
        {/* Timeline Nodes */}
        <div className="relative z-10 flex justify-between">
          {states.map((state, idx) => (
            <div key={state} className={`w-10 h-10 rounded-full flex items-center justify-center ${idx <= currentIndex ? 'bg-primary text-white shadow-[0_0_15px_rgba(255,0,127,0.8)]' : 'bg-gray-800 text-gray-500'} transition-colors duration-500`}>
              {idx === 0 ? <Clock size={20} /> : idx === 3 ? <Truck size={20} /> : idx === 4 ? <CheckCircle size={20} /> : <Package size={20} />}
            </div>
          ))}
        </div>
      </div>
      
      {/* Textual Status */}
      <div className="text-center mb-10">
        <h3 className="text-2xl font-bold text-accent">{order.status.replace(/_/g, ' ')}</h3>
      </div>

      {/* Dispute Mechanism (only visible when order is marked delivered) */}
      {(order.status === 'DELIVERED' || currentIndex >= 4) && (
        <div className="mt-8 border-t border-gray-700 pt-6 text-center">
          <p className="text-gray-400 mb-4">Didn't receive your order?</p>
          <button onClick={reportNotReceived} className="bg-red-500/20 text-red-400 px-6 py-2 rounded-lg hover:bg-red-500/40 transition-colors">
            Report Not Received
          </button>
        </div>
      )}
    </div>
  );
}
