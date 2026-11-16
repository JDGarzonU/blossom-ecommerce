export const product = {
  id: 1,
  name: 'Teclado Mecánico RGB',
  description: 'Teclado para gaming',
  price: 189900,
  category: 'Perifericos',
  stock: 15,
  active: true,
  imageUrl: null,
};

export const authResponse = {
  accessToken: 'unit-test-token',
  tokenType: 'Bearer',
  userId: 1,
  name: 'Cliente Prueba',
  email: 'cliente@blossom.test',
  role: 'CUSTOMER',
};

export const order = {
  id: 1,
  status: 'CREATED',
  createdAt: '2026-11-16T10:52:14',
  total: 379800,
  items: [{
    productId: 1,
    productName: 'Teclado Mecánico RGB',
    quantity: 2,
    unitPrice: 189900,
    subtotal: 379800,
  }],
};

export function customerSession() {
  sessionStorage.setItem('blossom_token', 'unit-test-token');
  sessionStorage.setItem('blossom_user', JSON.stringify({
    id: 1, name: 'Cliente Prueba', email: 'cliente@blossom.test', role: 'CUSTOMER',
  }));
}
