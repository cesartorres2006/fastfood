// src/main/resources/static/js/validation.js
document.addEventListener('DOMContentLoaded', function() {
    // Validación del formulario de registro
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', function(event) {
            if (!validateRegisterForm()) {
                event.preventDefault();
            }
        });
    }
});

function validateRegisterForm() {
    const username = document.getElementById('username').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const fullName = document.getElementById('fullName').value.trim();

    // Validar nombre de usuario
    if (username.length < 3) {
        alert('El nombre de usuario debe tener al menos 3 caracteres');
        return false;
    }

    // Validar email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        alert('Por favor, ingresa un correo electrónico válido');
        return false;
    }

    // Validar contraseña
    if (password.length < 6) {
        alert('La contraseña debe tener al menos 6 caracteres');
        return false;
    }

    // Validar nombre completo
    if (fullName.length < 3) {
        alert('Por favor, ingresa tu nombre completo');
        return false;
    }

    return true;
}
