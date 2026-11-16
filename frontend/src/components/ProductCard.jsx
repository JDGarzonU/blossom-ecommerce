import { Link } from 'react-router-dom';
import { ShoppingCart, ArrowUpRight } from 'lucide-react';
import { useCart } from '../context/CartContext';
import { money } from '../utils';

export default function ProductCard({ product }) {
  const { add } = useCart();
  return <article className="product-card">
    <Link to={`/products/${product.id}`} className="product-image">
      {product.imageUrl ? <img src={product.imageUrl} alt={product.name} loading="lazy" onError={(e) => {e.currentTarget.style.display='none';}}/> : <span className="image-fallback">BLOSSOM</span>}
      <span className="image-link"><ArrowUpRight size={17}/></span>
    </Link>
    <div className="product-body">
      <span className="eyebrow">{product.category}</span>
      <Link to={`/products/${product.id}`} className="product-name">{product.name}</Link>
      <p className="product-description">{product.description || 'Producto disponible en nuestra tienda.'}</p>
      <div className="product-price">{money(product.price)}</div>
      <div className="product-bottom"><span className={product.stock > 0 ? 'stock-ok' : 'stock-out'}>{product.stock > 0 ? `${product.stock} disponibles` : 'Agotado'}</span><button className="btn btn-primary btn-small" disabled={!product.stock} onClick={() => add(product)} aria-label={`Agregar ${product.name} al carrito`}><ShoppingCart size={16}/> Agregar</button></div>
    </div>
  </article>;
}
