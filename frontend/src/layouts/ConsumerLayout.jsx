import { Outlet, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LogOut, ShoppingCart } from 'lucide-react';

export default function ConsumerLayout() {
  const { logout, user } = useAuth();
  return (
    <div className="min-h-screen flex flex-col relative overflow-hidden">
      {/* Background decorations */}
      <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-primary rounded-full mix-blend-multiply filter blur-[100px] opacity-20 animate-blob"></div>
      <div className="absolute top-[20%] right-[-10%] w-[40%] h-[40%] bg-secondary rounded-full mix-blend-multiply filter blur-[100px] opacity-20 animate-blob animation-delay-2000"></div>
      <div className="absolute bottom-[-10%] left-[20%] w-[40%] h-[40%] bg-accent rounded-full mix-blend-multiply filter blur-[100px] opacity-20 animate-blob animation-delay-4000"></div>
      
      <header className="glass m-4 p-4 flex justify-between items-center z-50 sticky top-4">
        <Link to="/consumer" className="text-2xl font-bold bg-unicorn-gradient bg-clip-text text-transparent">
          Qwickie
        </Link>
        <div className="flex items-center gap-6">
          <span className="text-gray-300 font-medium hidden sm:block">Hi, {user.username}</span>
          <Link to="/consumer/checkout" className="hover:text-primary transition-colors flex items-center gap-2">
            <ShoppingCart size={20} /> <span className="hidden sm:inline">Cart</span>
          </Link>
          <button onClick={logout} className="hover:text-primary transition-colors">
            <LogOut size={20} />
          </button>
        </div>
      </header>
      <main className="flex-1 p-4 w-full max-w-6xl mx-auto z-10">
        <Outlet />
      </main>
    </div>
  );
}
