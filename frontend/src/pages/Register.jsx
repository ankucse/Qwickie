import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Link, useNavigate } from 'react-router-dom';

export default function Register() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState('');
  const [role, setRole] = useState('CUSTOMER');
  const [error, setError] = useState('');
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await register(username, password, phone, role);
      navigate(role === 'CUSTOMER' ? '/consumer' : '/partner');
    } catch (err) {
      setError('Registration failed. Try a different username.');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center relative overflow-hidden bg-darker">
      <div className="absolute w-96 h-96 bg-primary rounded-full mix-blend-multiply filter blur-[128px] opacity-20 top-1/4 left-1/4 animate-blob"></div>
      
      <div className="glass-card p-10 w-full max-w-md z-10">
        <h2 className="text-4xl font-bold mb-8 text-center bg-unicorn-gradient bg-clip-text text-transparent">Join Qwickie</h2>
        {error && <div className="bg-red-500/20 text-red-400 p-3 rounded mb-4 text-center">{error}</div>}
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div>
            <label className="block text-gray-400 text-sm mb-1">Username</label>
            <input type="text" className="w-full bg-dark border border-gray-700 rounded-lg p-3 text-white focus:border-primary outline-none" value={username} onChange={e => setUsername(e.target.value)} required />
          </div>
          <div>
            <label className="block text-gray-400 text-sm mb-1">Password</label>
            <input type="password" className="w-full bg-dark border border-gray-700 rounded-lg p-3 text-white focus:border-primary outline-none" value={password} onChange={e => setPassword(e.target.value)} required />
          </div>
          <div>
            <label className="block text-gray-400 text-sm mb-1">Phone</label>
            <input type="text" className="w-full bg-dark border border-gray-700 rounded-lg p-3 text-white focus:border-primary outline-none" value={phone} onChange={e => setPhone(e.target.value)} required />
          </div>
          <div>
            <label className="block text-gray-400 text-sm mb-1">Role</label>
            <select className="w-full bg-dark border border-gray-700 rounded-lg p-3 text-white focus:border-primary outline-none" value={role} onChange={e => setRole(e.target.value)}>
              <option value="CUSTOMER">Customer</option>
              <option value="RIDER">Delivery Rider</option>
            </select>
          </div>
          <button type="submit" className="btn-primary mt-4">Register</button>
        </form>
        <p className="mt-6 text-center text-gray-400">
          Already have an account? <Link to="/login" className="text-primary hover:underline">Login</Link>
        </p>
      </div>
    </div>
  );
}
