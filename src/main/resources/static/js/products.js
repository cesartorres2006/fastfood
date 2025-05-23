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
            }, 350); // 350 ms de espera después de dejar de escribir
        });
    }

    // Delegación global para botones + y - (funciona siempre)
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('btn-decrement')) {
            const id = e.target.getAttribute('data-id');
            const input = document.getElementById(`cantidad-${id}`);
            if (input) {
                let val = parseInt(input.value) || 1;
                if (val > 1) input.value = val - 1;
            }
        } else if (e.target.classList.contains('btn-increment')) {
            const id = e.target.getAttribute('data-id');
            const input = document.getElementById(`cantidad-${id}`);
            if (input) {
                let val = parseInt(input.value) || 1;
                if (val < 20) input.value = val + 1;
            }
        }
    });

    // Bloquea cualquier intento de agregar al carrito en la vista admin
    if (window.location.pathname.startsWith('/admin')) {
        document.addEventListener('click', function(e) {
            if (e.target.classList.contains('add-to-cart')) {
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

    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al cargar los productos');
            }
            return response.json();
        })
        .then(products => {
            displayProducts(products);
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Ha ocurrido un error al cargar los productos. Por favor, inténtalo de nuevo.');
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
        container.innerHTML = '<div class="col-12"><div class="alert alert-info">No se encontraron productos que coincidan con tu búsqueda.</div></div>';
        return;
    }

    // Mostrar los productos
    products.forEach(product => {
        const productCard = document.createElement('div');
        productCard.className = 'col';

        // IDs y data-id únicos por producto
        const cantidadId = `cantidad-${product.id}`;
        productCard.innerHTML = `
            <div class="card h-100">
                <img src="${product.imageUrl || 'https://via.placeholder.com/300x200'}" class="card-img-top" alt="Imagen de ${product.name}">
                <div class="card-body">
                    <h5 class="card-title">${product.name}</h5>
                    <p class="card-text">${product.description || ''}</p>
                    <p class="card-text"><strong>${product.price.toLocaleString('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0, maximumFractionDigits: 0 })}</strong></p>
                    <div class="d-flex align-items-center gap-2 mt-2">
                        <div class="input-group flex-nowrap" style="max-width:120px;">
                            <button class="btn btn-outline-secondary btn-decrement" type="button" data-id="${product.id}">-</button>
                            <input type="number" class="form-control cantidad-input text-center" id="${cantidadId}" data-id="${product.id}" min="1" max="20" value="1" style="width:50px;">
                            <button class="btn btn-outline-secondary btn-increment" type="button" data-id="${product.id}">+</button>
                        </div>
                        <button class="btn btn-primary add-to-cart" data-id="${product.id}" data-name="${product.name}" data-price="${product.price}">
                            Añadir al Carrito
                        </button>
                    </div>
                </div>
            </div>
        `;

        container.appendChild(productCard);
    });

    // Volver a agregar los event listeners para los botones
    const addToCartButtons = document.querySelectorAll('.add-to-cart');
    addToCartButtons.forEach(button => {
        button.addEventListener('click', function() {
            const productId = this.getAttribute('data-id');
            const cantidadInput = document.getElementById(`cantidad-${productId}`);
            let cantidad = 1;
            if (cantidadInput) {
                cantidad = parseInt(cantidadInput.value) || 1;
            }
            addToCart(productId, cantidad);
        });
    });
}
