import { useEffect, useState } from 'react';
import { Search, SlidersHorizontal, RefreshCcw } from 'lucide-react';
import api, { getErrorMessage } from '../services/api';
import ProductCard from '../components/ProductCard';

const initial = { name: '', category: '', minPrice: '', maxPrice: '' };
export default function Catalog() {
  const [filters, setFilters] = useState(initial);
  const [applied, setApplied] = useState(initial);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [retry, setRetry] = useState(0);
  useEffect(() => {
    const controller = new AbortController();
    const params = Object.fromEntries(Object.entries(applied).filter(([, value]) => String(value).trim() !== ''));
    api.get('/products', { params, signal: controller.signal })
      .then(({ data }) => { if (!controller.signal.aborted) setProducts(Array.isArray(data) ? data : []); })
      .catch((err) => { if (!controller.signal.aborted && err.code !== 'ERR_CANCELED') setError(getErrorMessage(err)); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [applied, retry]);
  function submit(e) { e.preventDefault(); setLoading(true); setError(''); setApplied({...filters}); }
  function clear() { setFilters(initial); setLoading(true); setError(''); setApplied({...initial}); }
  function retryLoad() { setLoading(true); setError(''); setRetry(n => n + 1); }
  return <>
    <section className="hero"><div className="hero-content"><span className="hero-kicker">TECNOLOGÍA PARA TU DÍA A DÍA</span><h1>Encuentra lo que <em>te inspira.</em></h1><p>Descubre productos que combinan funcionalidad, diseño y estilo. Todo en un solo lugar.</p><a href="#catalogo" className="btn btn-light">Explorar catálogo <span>↗</span></a></div><div className="hero-art"><span className="hero-orbit orbit-one"/><span className="hero-orbit orbit-two"/><span className="hero-shape">✳</span></div></section>
    <section className="section-heading" id="catalogo"><div><span className="eyebrow">NUESTRA SELECCIÓN</span><h2>Explora nuestros productos</h2><p>Encuentra exactamente lo que estás buscando.</p></div><span className="result-count">{loading ? 'Cargando…' : `${products.length} productos`}</span></section>
    <form className="filters" onSubmit={submit}><div className="filter-title"><SlidersHorizontal size={18}/><strong>Filtros</strong></div><label className="field"><span>Nombre</span><div className="input-icon"><Search size={17}/><input placeholder="Buscar productos" value={filters.name} onChange={e=>setFilters({...filters,name:e.target.value})}/></div></label><label className="field"><span>Categoría</span><input placeholder="Ej. Perifericos" value={filters.category} onChange={e=>setFilters({...filters,category:e.target.value})}/></label><label className="field"><span>Precio mínimo</span><input type="number" min="0" step="0.01" placeholder="$0" value={filters.minPrice} onChange={e=>setFilters({...filters,minPrice:e.target.value})}/></label><label className="field"><span>Precio máximo</span><input type="number" min="0" step="0.01" placeholder="Sin límite" value={filters.maxPrice} onChange={e=>setFilters({...filters,maxPrice:e.target.value})}/></label><div className="filter-buttons"><button className="btn btn-primary" type="submit">Buscar</button><button className="btn btn-quiet icon-button" type="button" onClick={clear} aria-label="Limpiar filtros"><RefreshCcw size={18}/></button></div></form>
    {error && <div className="alert error" role="alert">{error}<button onClick={retryLoad}>Reintentar</button></div>}
    {loading ? <div className="empty-state">Cargando productos…</div> : !error && (products.length ? <div className="product-grid">{products.map(p=><ProductCard key={p.id} product={p}/>)}</div> : <div className="empty-state"><Search size={30}/><h3>Sin resultados</h3><p>Prueba con otros filtros o elimina las condiciones de búsqueda.</p><button className="btn btn-outline" onClick={clear}>Limpiar filtros</button></div>)}
  </>;
}
