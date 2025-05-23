document.addEventListener("DOMContentLoaded", function() {
    const buttons = document.querySelectorAll(".agenda-btn");
    const salas = document.querySelectorAll(".agenda-sala");

    buttons.forEach(button => {
        button.addEventListener("click", function() {
            const salaId = this.getAttribute("data-id");

            // Oculta todas las salas
            salas.forEach(sala => sala.style.display = "none");

            // Muestra la sala seleccionada
            document.getElementById(salaId).style.display = "block";

            // Remueve la clase "active" de todos los botones
            buttons.forEach(btn => btn.classList.remove("active"));

            // Agrega la clase "active" al botón seleccionado
            this.classList.add("active");
        });
    });

    // Marcar la primera sala como activa al cargar la página
    if (buttons.length > 0) {
        buttons[0].classList.add("active");
        salas[0].style.display = "block";
    }
});
