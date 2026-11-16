import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { ShoppingBag, ShoppingCart, LogIn, LogOut, Package, UserRound } from 'lucide-react';
import { useAuth } from '../context/useAuth';
import { useCart } from '../context/useCart';

export default function Layout() {
  const { user, logout } = useAuth();
  const { count } = useCart();
  const navigate = useNavigate();
  function leave() { logout(); navigate('/'); }
  return <div className="app-shell">
    <header className="site-header">
      <div className="container nav-inner">
        <Link className="brand" to="/"><ShoppingBag size={25}/><span>BLOSSOM<span className="brand-dot">.</span></span></Link>
        <nav className="nav-links" aria-label="Navegación principal">
          <NavLink to="/">Catálogo</NavLink>
          {user && <NavLink to="/orders">Mis pedidos</NavLink>}
        </nav>
        <div className="nav-actions">
          <Link className="cart-action" to="/cart" aria-label={`Carrito, ${count} artículos`}><ShoppingCart size={20}/><span>Carrito</span>{count > 0 && <span className="cart-count">{count}</span>}</Link>
          {user ? <><span className="username"><UserRound size={17}/>{user.name.split(' ')[0]}</span><button className="btn btn-quiet btn-small" onClick={leave}><LogOut size={16}/> Salir</button></>
            : <Link className="btn btn-primary btn-small" to="/login"><LogIn size={16}/> Ingresar</Link>}
        </div>
      </div>
    </header>
    <main className="container main-content"><Outlet/></main>
    <footer className="footer"><div className="container"><span>© {new Date().getFullYear()} Blossom E-commerce · Prueba técnica</span><span><Package size={15}/> Java + Spring Boot + React</span></div></footer>
  </div>;
}
