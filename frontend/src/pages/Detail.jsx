import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft, ShoppingCart, Minus, Plus } from 'lucide-react';
import api, { getErrorMessage } from '../services/api';
import { useCart } from '../context/CartContext';
import { money } from '../utils';
export default function Detail() {
  const { id } = useParams(); const [product,setProduct] = useState(null); const [error,setError] = useState(''); const [loading,setLoading] = useState(true); const [quantity,setQuantity] = useState(1); const [added,setAdded] = useState(false); const { add }=useCart();
  useEffect(()=>{ let alive=true; setLoading(true); api.get(`/products/${id}`).then(({data})=>{if(alive)setProduct(data);}).catch(e=>{if(alive)setError(getErrorMessage(e));}).finally(()=>{if(alive)setLoading(false);}); return()=>{alive=false;}; },[id]);
  if (loading) return <div className="empty-state">Cargando producto…</div>;
  if (error || !product) return <div className="empty-state"><h2>No se pudo cargar el producto</h2><p>{error}</p><Link to="/" className="btn btn-outline">Volver al catálogo</Link></div>;
  return <><Link className="back-link" to="/"><ArrowLeft size={18}/> Volver al catálogo</Link><section className="detail-grid"><div className="detail-image">{product.imageUrl ? <img src={product.imageUrl} alt={product.name}/> : <span>BLOSSOM</span>}</div><div className="detail-content"><span className="eyebrow">{product.category}</span><h1>{product.name}</h1><p className="detail-copy">{product.description || 'Producto disponible en nuestra tienda.'}</p><div className="detail-price">{money(product.price)}</div><p className={product.stock>0?'stock-ok':'stock-out'}>{product.stock>0?`${product.stock} unidades en inventario`:'Producto agotado'}</p><div className="detail-actions"><div className="quantity-control"><button disabled={quantity<=1} onClick={()=>setQuantity(q=>q-1)} aria-label="Restar cantidad"><Minus size={17}/></button><span>{quantity}</span><button disabled={quantity>=product.stock} onClick={()=>setQuantity(q=>q+1)} aria-label="Sumar cantidad"><Plus size={17}/></button></div><button className="btn btn-primary" disabled={!product.stock} onClick={()=>{add(product,quantity);setAdded(true);}}><ShoppingCart size={18}/> Agregar al carrito</button></div>{added&&<p className="success-note">Añadido al carrito. <Link to="/cart">Ver carrito</Link></p>}</div></section></>;
}
