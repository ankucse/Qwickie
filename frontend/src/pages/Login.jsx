import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Link, useNavigate } from 'react-router-dom';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const role = await login(username, password);
      navigate(role === 'CUSTOMER' ? '/consumer' : '/partner');
    } catch (err) {
      setError('Invalid credentials');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center relative overflow-hidden bg-darker">
      <div className="absolute w-96 h-96 bg-primary rounded-full mix-blend-multiply filter blur-[128px] opacity-20 top-1/4 left-1/4 animate-blob"></div>
      <div className="absolute w-96 h-96 bg-secondary rounded-full mix-blend-multiply filter blur-[128px] opacity-20 bottom-1/4 right-1/4 animate-blob animation-delay-2000"></div>
      
      <div className="glass-card p-10 w-full max-w-md z-10">
        <h2 className="text-4xl font-bold mb-8 text-center bg-unicorn-gradient bg-clip-text text-transparent">Qwickie</h2>
        {error && <div className="bg-red-500/20 text-red-400 p-3 rounded mb-4 text-center">{error}</div>}
        <form onSubmit={handleSubmit} className="flex flex-col gap-5">
          <div>
            <label className="block text-gray-400 text-sm mb-2">Username</label>
            <input type="text" className="w-full bg-dark border border-gray-700 rounded-lg p-3 text-white focus:outline-none focus:border-primary transition-colors" value={username} onChange={e => setUsername(e.target.value)} required />
          </div>
          <div>
            <label className="block text-gray-400 text-sm mb-2">Password</label>
            <input type="password" className="w-full bg-dark border border-gray-700 rounded-lg p-3 text-white focus:outline-none focus:border-primary transition-colors" value={password} onChange={e => setPassword(e.target.value)} required />
          </div>
          <button type="submit" className="btn-primary mt-4">Login</button>
        </form>
        <p className="mt-6 text-center text-gray-400">
          Don't have an account? <Link to="/register" className="text-primary hover:underline">Register</Link>
        </p>
      </div>
    </div>
  );
}
