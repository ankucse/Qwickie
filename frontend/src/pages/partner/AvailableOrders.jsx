import { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

export default function AvailableOrders() {
  const [orders, setOrders] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    fetchOrders();
    const interval = setInterval(fetchOrders, 5000);
    return () => clearInterval(interval);
  }, []);

  const fetchOrders = () => {
    axios.get('http://localhost:8080/api/orders/available').then(res => setOrders(res.data));
  };

  const acceptOrder = async (id) => {
    await axios.post(`http://localhost:8080/api/orders/${id}/accept`);
    navigate(`/partner/order/${id}`);
  };

  return (
    <div>
      <h2 className="text-3xl font-bold mb-6 text-accent">Available Deliveries</h2>
      {orders.length === 0 ? (
        <p className="text-gray-400">Waiting for new orders...</p>
      ) : (
        <div className="grid gap-4">
          {orders.map(o => (
            <div key={o.id} className="glass p-6 flex justify-between items-center">
              <div>
                <h4 className="text-xl font-bold mb-1">Order #{o.id}</h4>
                <p className="text-gray-400 text-sm">Pincode: {o.pincode}</p>
                <p className="text-gray-400 text-sm">Address: {o.deliveryAddress}</p>
              </div>
              <button onClick={() => acceptOrder(o.id)} className="bg-accent text-dark font-bold px-6 py-2 rounded hover:opacity-80 transition-opacity">
                Accept
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
