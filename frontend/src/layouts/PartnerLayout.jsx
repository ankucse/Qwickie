import { Outlet, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LogOut } from 'lucide-react';

export default function PartnerLayout() {
  const { logout, user } = useAuth();
  return (
    <div className="min-h-screen flex flex-col bg-[#0a0a0a]">
      <header className="glass m-4 p-4 flex justify-between items-center z-50 sticky top-4 border-accent/30">
        <Link to="/partner" className="text-2xl font-bold text-accent">
          Qwickie Rider
        </Link>
        <div className="flex items-center gap-6">
          <span className="text-gray-300">Rider: {user.username}</span>
          <button onClick={logout} className="hover:text-accent transition-colors">
            <LogOut size={20} />
          </button>
        </div>
      </header>
      <main className="flex-1 p-4 w-full max-w-6xl mx-auto">
        <Outlet />
      </main>
    </div>
  );
}
