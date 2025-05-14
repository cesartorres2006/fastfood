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
        searchButton.addEventListener('click', function() {
            filterProducts();
        });
    }

    const searchInput = document.getElementById('search-input');
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                filterProducts();
            }
        });
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

        productCard.innerHTML = `
            <div class="card h-100">
                <img src="${product.imageUrl || 'https://via.placeholder.com/300x200'}" class="card-img-top" alt="Imagen de ${product.name}">
                <div class="card-body">
                    <h5 class="card-title">${product.name}</h5>
                    <p class="card-text">${product.description || ''}</p>
                    <p class="card-text"><strong>$${product.price.toFixed(2)}</strong></p>
                    <button class="btn btn-primary add-to-cart" data-id="${product.id}" data-name="${product.name}" data-price="${product.price}">
                        Añadir al Carrito
                    </button>
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
            const productName = this.getAttribute('data-name');
            const productPrice = parseFloat(this.getAttribute('data-price'));

            addToCart(productId, productName, productPrice);
        });
    });
}
