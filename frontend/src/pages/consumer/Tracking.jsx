import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Package, Truck, CheckCircle, Clock } from 'lucide-react';

export default function Tracking() {
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    // Fetch initial state
    axios.get(`http://localhost:8080/api/orders/${id}`).then(res => setOrder(res.data));

    // Listen to SSE
    const token = localStorage.getItem('token');
    const sse = new EventSource(`http://localhost:8080/api/orders/${id}/stream?token=${token}`);
    sse.addEventListener("status-update", (e) => {
      setOrder(prev => prev ? { ...prev, status: e.data } : null);
    });

    return () => sse.close();
  }, [id]);

  const reportNotReceived = async () => {
    await axios.post(`http://localhost:8080/api/orders/${id}/not-received`);
    navigate('/consumer/support');
  };

  if (!order) return <div className="text-center mt-10">Loading tracking...</div>;

  const states = ['ORDER_RECEIVED', 'PARTNER_ACCEPTED', 'EN_ROUTE_TO_PICKUP', 'EN_ROUTE_TO_DELIVERY', 'DELIVERED'];
  const currentIndex = states.indexOf(order.status);
  
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
      
      <div className="relative mb-12">
        <div className="absolute top-1/2 left-0 w-full h-1 bg-gray-700 -translate-y-1/2 z-0"></div>
        <div className="absolute top-1/2 left-0 h-1 bg-primary -translate-y-1/2 z-0 transition-all duration-500" style={{ width: `${(currentIndex / (states.length - 1)) * 100}%` }}></div>
        
        <div className="relative z-10 flex justify-between">
          {states.map((state, idx) => (
            <div key={state} className={`w-10 h-10 rounded-full flex items-center justify-center ${idx <= currentIndex ? 'bg-primary text-white shadow-[0_0_15px_rgba(255,0,127,0.8)]' : 'bg-gray-800 text-gray-500'} transition-colors duration-500`}>
              {idx === 0 ? <Clock size={20} /> : idx === 3 ? <Truck size={20} /> : idx === 4 ? <CheckCircle size={20} /> : <Package size={20} />}
            </div>
          ))}
        </div>
      </div>
      
      <div className="text-center mb-10">
        <h3 className="text-2xl font-bold text-accent">{order.status.replace(/_/g, ' ')}</h3>
      </div>

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
