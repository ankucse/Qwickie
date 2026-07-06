import { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

export default function Products() {
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState({});
  const [activeCategory, setActiveCategory] = useState('All');
  const navigate = useNavigate();

  useEffect(() => {
    axios.get('http://localhost:8080/api/products').then(res => setProducts(res.data));
  }, []);

  const addToCart = (p) => {
    setCart(prev => ({ ...prev, [p.id]: (prev[p.id] || 0) + 1, [`_data_${p.id}`]: p }));
  };

  const handleCheckout = () => {
    const items = Object.keys(cart).filter(k => !k.startsWith('_')).map(id => ({
      product: cart[`_data_${id}`],
      quantity: cart[id]
    }));
    if(items.length > 0) {
      localStorage.setItem('cart', JSON.stringify(items));
      navigate('/consumer/checkout');
    }
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-white">Fresh Groceries</h1>
        <button onClick={handleCheckout} className="btn-primary">
          Checkout ({Object.keys(cart).filter(k => !k.startsWith('_')).reduce((acc, curr) => acc + cart[curr], 0)} items)
        </button>
      </div>
      
      {/* Category Filter Navigation */}
      <div className="flex flex-wrap gap-3 mb-8">
        <button 
          onClick={() => setActiveCategory('All')} 
          className={`px-5 py-2 rounded-full font-medium transition-all ${activeCategory === 'All' ? 'bg-primary text-white shadow-[0_0_15px_rgba(255,0,127,0.4)]' : 'bg-dark/50 text-gray-400 border border-gray-700 hover:border-primary/50'}`}
        >
          All
        </button>
        {Array.from(new Set(products.map(p => p.category))).map(category => (
          <button 
            key={category}
            onClick={() => setActiveCategory(category)}
            className={`px-5 py-2 rounded-full font-medium transition-all ${activeCategory === category ? 'bg-primary text-white shadow-[0_0_15px_rgba(255,0,127,0.4)]' : 'bg-dark/50 text-gray-400 border border-gray-700 hover:border-primary/50'}`}
          >
            {category}
          </button>
        ))}
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
        {products.filter(p => activeCategory === 'All' || p.category === activeCategory).map(p => (
          <div key={p.id} className="glass-card overflow-hidden hover-scale flex flex-col">
            <div className="h-48 overflow-hidden">
              <img src={p.imageUrl} alt={p.name} className="w-full h-full object-cover" />
            </div>
            <div className="p-6 flex-1 flex flex-col">
              <h3 className="text-xl font-bold text-accent mb-2">{p.name}</h3>
              <p className="text-gray-400 text-sm mb-4 flex-1">{p.description}</p>
              <div className="flex justify-between items-center mt-auto relative z-10">
                <span className="text-2xl font-bold">₹{p.price.toFixed(2)}</span>
                <button onClick={() => addToCart(p)} className="bg-primary hover:bg-secondary text-white px-4 py-2 rounded-lg transition-colors font-medium">
                  {cart[p.id] ? `Add (${cart[p.id]})` : 'Add'}
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
