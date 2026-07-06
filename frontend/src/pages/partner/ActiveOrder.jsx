import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function ActiveOrder() {
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    fetchOrder();
  }, [id]);

  const fetchOrder = () => {
    axios.get(`http://localhost:8080/api/orders/${id}`).then(res => setOrder(res.data));
  };

  const updateStatus = async (status) => {
    await axios.post(`http://localhost:8080/api/orders/${id}/status`, { status });
    if (status === 'DELIVERED') {
      navigate('/partner');
    } else {
      fetchOrder();
    }
  };

  if (!order) return <div>Loading...</div>;

  return (
    <div className="max-w-2xl mx-auto glass p-8">
      <h2 className="text-3xl font-bold mb-6 text-accent">Active Delivery: #{order.id}</h2>
      
      <div className="mb-8 p-4 bg-dark rounded-lg">
        <p><strong>Pincode:</strong> {order.pincode}</p>
        <p><strong>Address:</strong> {order.deliveryAddress}</p>
        <p><strong>Current Status:</strong> <span className="text-primary font-bold">{order.status.replace(/_/g, ' ')}</span></p>
      </div>

      <div className="flex flex-col gap-3">
        {order.status === 'PARTNER_ACCEPTED' && (
          <button onClick={() => updateStatus('EN_ROUTE_TO_PICKUP')} className="btn-primary w-full">En Route to Pickup</button>
        )}
        {order.status === 'EN_ROUTE_TO_PICKUP' && (
          <button onClick={() => updateStatus('EN_ROUTE_TO_DELIVERY')} className="btn-primary w-full">Order Picked Up (En Route to Delivery)</button>
        )}
        {order.status === 'EN_ROUTE_TO_DELIVERY' && (
          <button onClick={() => updateStatus('DELIVERED')} className="btn-primary w-full">Mark as Delivered</button>
        )}
      </div>
    </div>
  );
}
