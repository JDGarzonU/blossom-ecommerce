export const money = (value) => new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(Number(value) || 0);
export const dateLabel = (date) => date ? new Date(date).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' }) : '—';
