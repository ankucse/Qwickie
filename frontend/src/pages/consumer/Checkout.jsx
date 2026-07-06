import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function Checkout() {
  const [items, setItems] = useState([]);
  const [pincode, setPincode] = useState('');
  const [address, setAddress] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const saved = localStorage.getItem('cart');
    if (saved) setItems(JSON.parse(saved));
  }, []);

  const totalAmount = items.reduce((sum, item) => sum + (item.product.price * item.quantity), 0);

  const handlePlaceOrder = async (e) => {
    e.preventDefault();
    try {
      const res = await axios.post('http://localhost:8080/api/orders', {
        totalAmount,
        pincode,
        deliveryAddress: address
      });
      localStorage.removeItem('cart');
      navigate(`/consumer/tracking/${res.data.id}`);
    } catch (err) {
      setError(err.response?.data?.error || 'Order failed. Please check your details.');
    }
  };

  return (
    <div className="max-w-2xl mx-auto glass p-8">
      <h2 className="text-3xl font-bold mb-6 text-accent">Checkout</h2>
      
      {items.length === 0 ? (
        <p>Your cart is empty.</p>
      ) : (
        <>
          <div className="mb-8 border-b border-gray-700 pb-4">
            {items.map(item => (
              <div key={item.product.id} className="flex justify-between items-center mb-2">
                <span>{item.quantity}x {item.product.name}</span>
                <span>₹{(item.product.price * item.quantity).toFixed(2)}</span>
              </div>
            ))}
            <div className="flex justify-between items-center mt-4 text-xl font-bold">
              <span>Total:</span>
              <span className="text-primary">₹{totalAmount.toFixed(2)}</span>
            </div>
          </div>

          {error && <div className="bg-red-500/20 text-red-400 p-3 rounded mb-4">{error}</div>}

          <form onSubmit={handlePlaceOrder} className="flex flex-col gap-4">
            <div>
              <label className="block text-gray-400 text-sm mb-1">Kolkata Pincode</label>
              <input type="text" placeholder="e.g. 700001" className="w-full bg-dark border border-gray-700 rounded-lg p-3 text-white focus:border-primary outline-none" value={pincode} onChange={e => setPincode(e.target.value)} required />
              <small className="text-gray-500">Must start with 700</small>
            </div>
            <div>
              <label className="block text-gray-400 text-sm mb-1">Delivery Address</label>
              <textarea className="w-full bg-dark border border-gray-700 rounded-lg p-3 text-white focus:border-primary outline-none min-h-[100px]" value={address} onChange={e => setAddress(e.target.value)} required></textarea>
            </div>
            
            <div className="mt-2">
              <label className="block text-gray-400 text-sm mb-2">Payment Method</label>
              <div className="flex items-center gap-2 bg-dark/50 p-4 border border-primary/50 rounded-lg">
                <input type="radio" id="cod" name="paymentMethod" value="COD" defaultChecked className="accent-primary w-4 h-4" />
                <label htmlFor="cod" className="text-white font-medium cursor-pointer">Cash on Delivery (COD)</label>
              </div>
            </div>

            <button type="submit" className="btn-primary mt-4">Place Order - ₹{totalAmount.toFixed(2)}</button>
          </form>
        </>
      )}
    </div>
  );
}
