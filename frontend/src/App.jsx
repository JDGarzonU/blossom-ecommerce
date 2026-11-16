import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import Catalog from './pages/Catalog';
import Detail from './pages/Detail';
import AuthPage from './pages/AuthPage';
import Cart from './pages/Cart';
import Orders from './pages/Orders';
export default function App(){return <BrowserRouter><AuthProvider><CartProvider><Routes><Route element={<Layout/>}><Route index element={<Catalog/>}/><Route path="products/:id" element={<Detail/>}/><Route path="login" element={<AuthPage mode="login"/>}/><Route path="register" element={<AuthPage mode="register"/>}/><Route path="cart" element={<Cart/>}/><Route path="orders" element={<ProtectedRoute><Orders/></ProtectedRoute>}/><Route path="*" element={<Navigate to="/" replace/>}/></Route></Routes></CartProvider></AuthProvider></BrowserRouter>}
