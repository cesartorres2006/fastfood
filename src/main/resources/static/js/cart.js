document.addEventListener('DOMContentLoaded', function() {
    // Inicializar el contador del carrito
    updateCartCount();

    // Eliminar listeners duplicados en los botones "Añadir al Carrito"
    const addToCartButtons = document.querySelectorAll('.add-to-cart');
    addToCartButtons.forEach(button => {
        button.replaceWith(button.cloneNode(true));
    });
    // Volver a agregar solo un listener por botón
    const freshAddToCartButtons = document.querySelectorAll('.add-to-cart');
    const isAdmin = document.body.getAttribute('data-role') === 'admin';
    // Solo asigna listeners si NO es la vista admin
    if (!isAdmin) {
        freshAddToCartButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                e.stopPropagation();
                e.preventDefault();
                const productId = this.getAttribute('data-id');
                const productName = this.getAttribute('data-name');
                const productPrice = parseFloat(this.getAttribute('data-price'));
                const quantityInput = this.parentNode.querySelector('input[type="number"]');
                const quantity = quantityInput ? parseInt(quantityInput.value) : 1;
                if (isAdmin) {
                    alert('El admin no puede realizar esta función');
                    return;
                }
                addToCart(productId, quantity, productName);
            });
        });
    } else {
        // Refuerzo: deshabilita y bloquea los botones en admin
        freshAddToCartButtons.forEach(btn => {
            btn.disabled = true;
            btn.onclick = null;
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                return false;
            }, true);
        });
    }

    // Si estamos en la página del carrito, cargar los items
    if (window.location.pathname === '/cart') {
        loadCartItems();
    }
});

function addToCart(id, quantity = 1, productName = undefined) {
    // Evita doble alerta si ya hay una en pantalla
    if (window._addingToCart) return;
    window._addingToCart = true;
    fetch(`/api/cart/add?productId=${id}&quantity=${quantity}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'same-origin'
    })
        .then(async response => {
            if (!response.ok) {
                let msg = 'La cantidad limite por producto es 20';
                try {
                    const errorData = await response.json();
                    if (errorData && errorData.error && errorData.error.includes('límite de productos')) {
                        msg = errorData.error;
                    }
                } catch (e) {}
                // alert(msg);
                showCartLimitModal();
                return Promise.reject(msg); // Solo un alert
            }
            return response.json();
        })
        .then(data => {
            updateCartCount();
            const name = data.productName || productName || 'Producto';
            showCartSuccessModal(quantity, name);
        })
        .catch(error => {
            console.error('Error:', error);
            if (error.message.includes('401')) {
                alert('Debes iniciar sesión para añadir productos al carrito.');
            } else {
                // No alert extra aquí
            }
        })
        .finally(() => {
            setTimeout(() => { window._addingToCart = false; }, 500);
        });
}

function updateCartCount() {
    // Llamar a la API para obtener el número de items en el carrito
    fetch('/api/cart/count', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'same-origin'
    })
        .then(response => response.json())
        .then(data => {
            const cartCountElement = document.getElementById('cart-count');
            if (cartCountElement) {
                cartCountElement.textContent = data.count;
            }
        })
        .catch(error => console.error('Error:', error));
}

function loadCartItems() {
    // Llamar a la API para obtener los items del carrito
    fetch('/api/cart', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'same-origin'
    })
        .then(response => {
            const contentType = response.headers.get('content-type');
            if (!response.ok) {
                if (contentType && contentType.includes('application/json')) {
                    return response.json().then(err => {
                        throw new Error(err.error || `Error al cargar el carrito: ${response.status}`);
                    });
                } else {
                    throw new Error(`Error al cargar el carrito: ${response.status}`);
                }
            }
            if (contentType && contentType.includes('application/json')) {
                return response.json();
            } else {
                return {}; // No se espera contenido útil
            }
        })
        .then(cart => {
            if (!cart || !cart.items) {
                // Si no hay items, probablemente hubo un error o respuesta inesperada
                return;
            }
            const cartItemsElement = document.getElementById('cart-items');
            const emptyCartMessage = document.getElementById('empty-cart-message');
            const cartContent = document.getElementById('cart-content');
            const cartTotalElement = document.getElementById('cart-total');
            const checkoutButton = document.getElementById('checkout-button');

            // Mostrar mensaje si el carrito está vacío
            if (cart.items.length === 0) {
                if (emptyCartMessage) emptyCartMessage.style.display = 'block';
                if (cartContent) cartContent.style.display = 'none';
                return;
            }

            // Ocultar mensaje si el carrito no está vacío
            if (emptyCartMessage) emptyCartMessage.style.display = 'none';
            if (cartContent) cartContent.style.display = 'block';

            // Limpiar el contenido actual
            if (cartItemsElement) cartItemsElement.innerHTML = '';

            // Agregar cada item al carrito
            cart.items.forEach(item => {
                const subtotal = item.product.price * item.quantity;

                if (cartItemsElement) {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                    <td>${item.product.name}</td>
                    <td>${item.product.price.toLocaleString('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0, maximumFractionDigits: 0 })}</td>
                    <td>
                        <div class="input-group input-group-sm" style="width: 120px;">
                            <button class="btn btn-outline-secondary decrease-quantity" data-id="${item.product.id}">-</button>
                            <input type="text" class="form-control text-center" value="${item.quantity}" readonly>
                            <button class="btn btn-outline-secondary increase-quantity" data-id="${item.product.id}">+</button>
                        </div>
                    </td>
                    <td>${subtotal.toLocaleString('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0, maximumFractionDigits: 0 })}</td>
                    <td>
                        <button class="btn btn-sm btn-danger remove-item" data-id="${item.product.id}">Eliminar</button>
                    </td>
                `;
                    cartItemsElement.appendChild(row);
                }
            });

            // Actualizar el total
            if (cartTotalElement) {
                cartTotalElement.textContent = cart.totalAmount.toLocaleString('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0, maximumFractionDigits: 0 });
            }

            // Agregar event listeners a los botones
            if (cartItemsElement) {
                // Botones para disminuir cantidad
                const decreaseButtons = cartItemsElement.querySelectorAll('.decrease-quantity');
                decreaseButtons.forEach(button => {
                    button.addEventListener('click', function() {
                        const productId = this.getAttribute('data-id');
                        updateQuantity(productId, -1);
                    });
                });

                // Botones para aumentar cantidad
                const increaseButtons = cartItemsElement.querySelectorAll('.increase-quantity');
                increaseButtons.forEach(button => {
                    button.addEventListener('click', function() {
                        const productId = this.getAttribute('data-id');
                        updateQuantity(productId, 1);
                    });
                });

                // Botones para eliminar items
                const removeButtons = cartItemsElement.querySelectorAll('.remove-item');
                removeButtons.forEach(button => {
                    button.addEventListener('click', function() {
                        const productId = this.getAttribute('data-id');
                        removeFromCart(productId);
                    });
                });
            }

            // Verificar si hay demasiados productos
            if (cart.totalItems > 10 && checkoutButton) {
                checkoutButton.disabled = true;
                // checkoutButton.title = 'Un pedido no puede tener más de 10 productos';
                // alert('Un pedido no puede tener más de 10 productos. Por favor, ajuste su carrito.');
            } else if (checkoutButton) {
                checkoutButton.disabled = false;
                checkoutButton.title = '';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Ha ocurrido un error al cargar el carrito.');
        });
}

function updateQuantity(productId, change) {
    // Obtener el valor actual
    const input = document.querySelector(`.increase-quantity[data-id="${productId}"]`).parentNode.querySelector('input');
    const currentQuantity = parseInt(input.value);
    const newQuantity = currentQuantity + change;

    // Llamar a la API para actualizar la cantidad
    fetch(`/api/cart/update?productId=${productId}&quantity=${newQuantity}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'same-origin'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al actualizar el carrito');
            }
            return response.json();
        })
        .then(data => {
            // Actualizar la vista
            updateCartCount();
            loadCartItems();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Ha ocurrido un error al actualizar el carrito.');
        });
}

function removeFromCart(productId) {
    // Llamar a la API para eliminar del carrito
    fetch(`/api/cart/remove?productId=${productId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'same-origin'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al eliminar del carrito');
            }
            return response.json();
        })
        .then(data => {
            // Actualizar la vista
            updateCartCount();
            loadCartItems();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Ha ocurrido un error al eliminar el producto del carrito.');
        });
}
