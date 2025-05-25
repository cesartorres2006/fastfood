// src/main/resources/static/js/products.js
document.addEventListener('DOMContentLoaded', function() {
    // Filtro por categoría
    const categoryFilter = document.getElementById('category-filter');
    if (categoryFilter) {
        categoryFilter.addEventListener('change', function() {
            filterProducts();
        });
    }

    // Búsqueda de productos
    const searchButton = document.getElementById('search-button');
    if (searchButton) {
        searchButton.addEventListener('click', function(e) {
            e.stopPropagation();
            e.preventDefault();
            filterProducts();
        });
    }

    const searchInput = document.getElementById('search-input');
    let debounceTimeout;
    if (searchInput) {
        // Búsqueda con Enter
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                filterProducts();
            }
        });
        // Búsqueda con debounce al escribir
        searchInput.addEventListener('input', function() {
            clearTimeout(debounceTimeout);
            debounceTimeout = setTimeout(() => {
                filterProducts();
            }, 350);
        });
    }

    // ===== DELEGACIÓN DE EVENTOS MEJORADA =====
    // Esta es la clave: usar delegación en el contenedor padre que nunca cambia
    document.addEventListener('click', function(e) {
        // Botón decrementar (-)
        if (e.target.closest('.btn-decrement')) {
            e.preventDefault();
            e.stopPropagation();

            const button = e.target.closest('.btn-decrement');
            const id = button.getAttribute('data-id');
            const input = document.getElementById(`cantidad-${id}`);

            if (input) {
                let val = parseInt(input.value) || 1;
                if (val > 1) {
                    input.value = val - 1;
                    // Disparar evento change para cualquier listener adicional
                    input.dispatchEvent(new Event('change'));
                }
            }
            return false;
        }

        // Botón incrementar (+)
        if (e.target.closest('.btn-increment')) {
            e.preventDefault();
            e.stopPropagation();

            const button = e.target.closest('.btn-increment');
            const id = button.getAttribute('data-id');
            const input = document.getElementById(`cantidad-${id}`);

            if (input) {
                let val = parseInt(input.value) || 1;
                if (val < 20) {
                    input.value = val + 1;
                    // Disparar evento change para cualquier listener adicional
                    input.dispatchEvent(new Event('change'));
                }
            }
            return false;
        }

        // Botón añadir al carrito
        if (e.target.closest('.add-to-cart')) {
            e.preventDefault();
            e.stopPropagation();

            const button = e.target.closest('.add-to-cart');
            const productId = button.getAttribute('data-id');
            const cantidadInput = document.getElementById(`cantidad-${productId}`);
            let cantidad = 1;

            if (cantidadInput) {
                cantidad = parseInt(cantidadInput.value) || 1;
            }

            // Verificar que addToCart existe antes de llamarla
            if (typeof addToCart === 'function') {
                addToCart(productId, cantidad);
            } else {
                console.error('Función addToCart no encontrada');
            }
            return false;
        }
    });

    // Delegación para inputs de cantidad (validación en tiempo real)
    document.addEventListener('input', function(e) {
        if (e.target.classList.contains('cantidad-input')) {
            let val = parseInt(e.target.value);
            if (isNaN(val) || val < 1) {
                e.target.value = 1;
            } else if (val > 20) {
                e.target.value = 20;
            }
        }
    });

    // Bloquea cualquier intento de agregar al carrito en la vista admin
    if (window.location.pathname.startsWith('/admin')) {
        document.addEventListener('click', function(e) {
            if (e.target.closest('.add-to-cart')) {
                e.preventDefault();
                e.stopPropagation();
                return false;
            }
        }, true);
    }
});

function filterProducts() {
    const category = document.getElementById('category-filter').value;
    const searchQuery = document.getElementById('search-input').value.trim();

    let url = '/api/products';

    if (category && searchQuery) {
        url = `/api/products/search?query=${encodeURIComponent(searchQuery)}&category=${encodeURIComponent(category)}`;
    } else if (category) {
        url = `/api/products/category/${encodeURIComponent(category)}`;
    } else if (searchQuery) {
        url = `/api/products/search?query=${encodeURIComponent(searchQuery)}`;
    }

    // Añadir efecto de carga
    const container = document.getElementById('products-container');
    container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>Buscando productos...</p></div>';

    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al cargar los productos');
            }
            return response.json();
        })
        .then(products => {
            setTimeout(() => {
                displayProducts(products);
            }, 300);
        })
        .catch(error => {
            console.error('Error:', error);
            container.innerHTML = '<div class="error-message"><i class="bi bi-exclamation-triangle"></i><p>Ha ocurrido un error al cargar los productos. Por favor, inténtalo de nuevo.</p></div>';
        });
}

function displayAdminProducts(products) {
    const tbody = document.querySelector('table.table tbody');
    if (!tbody) return;
    tbody.innerHTML = '';
    if (!products.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">No se encontraron productos.</td></tr>';
        return;
    }
    products.forEach(product => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${product.id}</td>
            <td><img src="${product.imageUrl ? product.imageUrl : 'https://via.placeholder.com/50'}" class="img-thumbnail" width="50" alt="Imagen del producto"></td>
            <td>${product.name}</td>
            <td>${product.category}</td>
            <td>$<span>${product.price}</span></td>
            <td>
                <span class="badge ${product.available ? 'bg-success' : 'bg-danger'}">${product.available ? 'Disponible' : 'No disponible'}</span>
            </td>
            <td>
                <div class="btn-group btn-group-sm">
                    <a href="/admin/products/edit/${product.id}" class="btn btn-outline-primary">Editar</a>
                    <a href="/admin/products/delete/${product.id}" class="btn btn-outline-danger delete-product" data-id="${product.id}" data-name="${product.name}">Eliminar</a>
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
    document.querySelectorAll('.delete-product').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            const productName = this.getAttribute('data-name');
            if (confirm(`¿Estás seguro de eliminar el producto "${productName}"?`)) {
                window.location.href = this.href;
            }
        });
    });
}

function displayProducts(products) {
    const container = document.getElementById('products-container');

    // Limpiar el contenedor
    container.innerHTML = '';

    if (products.length === 0) {
        container.innerHTML = `
            <div class="no-products-found">
                <div class="no-products-content">
                    <i class="bi bi-search"></i>
                    <h3>No se encontraron productos</h3>
                    <p>No hay productos que coincidan con tu búsqueda.</p>
                    <button class="btn-clear-search" onclick="clearSearch()">
                        <i class="bi bi-arrow-clockwise me-2"></i>
                        Ver todos los productos
                    </button>
                </div>
            </div>
        `;
        return;
    }

    // Mostrar los productos con el nuevo diseño elegante
    products.forEach((product, index) => {
        const productCard = document.createElement('div');
        productCard.className = 'product-card-wrapper';
        productCard.style.animationDelay = `${index * 0.1}s`;

        productCard.innerHTML = `
            <div class="product-card">
                <div class="card-image-wrapper">
                    <img src="${product.imageUrl || 'https://via.placeholder.com/300x200'}" 
                         class="product-image" alt="Imagen de ${product.name}">
                    <div class="image-overlay">
                        <div class="overlay-content">
                            <i class="bi bi-eye"></i>
                        </div>
                    </div>
                </div>
                
                <div class="card-content">
                    <div class="product-header">
                        <h5 class="product-title">${product.name}</h5>
                        <div class="title-decoration"></div>
                    </div>
                    
                    <p class="product-description">${product.description || 'Delicioso producto de nuestra cocina'}</p>
                    
                    <div class="price-section">
                        <span class="price-label">Precio</span>
                        <span class="product-price">
                            $${product.price.toLocaleString('es-CO', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}
                        </span>
                    </div>

                    <div class="product-actions">
                        <div class="quantity-controls">
                            <button class="quantity-btn btn-decrement" type="button" data-id="${product.id}">
                                <i class="bi bi-dash"></i>
                            </button>
                            <input type="number" class="quantity-input cantidad-input"
                                   id="cantidad-${product.id}" min="1" max="20" value="1">
                            <button class="quantity-btn btn-increment" type="button" data-id="${product.id}">
                                <i class="bi bi-plus"></i>
                            </button>
                        </div>
                        
                        <button class="add-to-cart-btn add-to-cart"
                                data-id="${product.id}" 
                                data-name="${product.name}" 
                                data-price="${product.price}">
                            <i class="bi bi-cart-plus me-2"></i>
                            <span>Añadir al Carrito</span>
                            <div class="btn-shine"></div>
                        </button>
                    </div>
                </div>
                
                <div class="card-decoration">
                    <div class="decoration-corner corner-top-left"></div>
                    <div class="decoration-corner corner-bottom-right"></div>
                </div>
            </div>
        `;

        container.appendChild(productCard);
    });

    // Añadir animación de entrada
    const cards = container.querySelectorAll('.product-card-wrapper');
    cards.forEach(card => {
        card.classList.add('fade-in-up');
    });

    // NO agregamos event listeners aquí - la delegación los maneja automáticamente
    console.log(`✅ Productos cargados: ${products.length}. Event listeners manejados por delegación.`);
}

// Función para limpiar la búsqueda
function clearSearch() {
    document.getElementById('search-input').value = '';
    document.getElementById('category-filter').value = '';
    filterProducts();
}

// Función de utilidad para debugging
function debugQuantityButtons() {
    console.log('=== DEBUG: Botones de cantidad ===');
    const decrementBtns = document.querySelectorAll('.btn-decrement');
    const incrementBtns = document.querySelectorAll('.btn-increment');
    const quantityInputs = document.querySelectorAll('.cantidad-input');

    console.log(`Botones decrementar encontrados: ${decrementBtns.length}`);
    console.log(`Botones incrementar encontrados: ${incrementBtns.length}`);
    console.log(`Inputs de cantidad encontrados: ${quantityInputs.length}`);

    // Verificar que todos tienen data-id
    decrementBtns.forEach((btn, index) => {
        const id = btn.getAttribute('data-id');
        console.log(`Botón decrementar ${index}: data-id = ${id}`);
    });
}
