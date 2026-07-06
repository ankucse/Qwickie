import { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    const username = localStorage.getItem('username');
    if (token && role) {
      setUser({ token, role, username });
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    }
    setLoading(false);
  }, []);

  const login = async (username, password) => {
    const res = await axios.post('http://localhost:8080/api/auth/login', { username, password });
    const { token, role } = res.data;
    localStorage.setItem('token', token);
    localStorage.setItem('role', role);
    localStorage.setItem('username', username);
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    setUser({ token, role, username });
    return role;
  };

  const register = async (username, password, phone, roleType) => {
    const res = await axios.post('http://localhost:8080/api/auth/register', { username, password, phone, role: roleType });
    const { token, role } = res.data;
    localStorage.setItem('token', token);
    localStorage.setItem('role', role);
    localStorage.setItem('username', username);
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    setUser({ token, role, username });
    return role;
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('username');
    delete axios.defaults.headers.common['Authorization'];
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, loading }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export default AuthProvider;
