// Función para filtrar semanas con ocupación
function filtrarSemanas() {
    const filtroActivo = document.getElementById('filtroSemanas').checked;
    const salas = document.querySelectorAll('.agenda-sala');
    
    salas.forEach(sala => {
        const semanas = sala.querySelectorAll('.agenda-table-wrapper');
        
        semanas.forEach(semana => {
            const celdasOcupadas = semana.querySelectorAll('.occupied, .blocked');
            
            if (filtroActivo && celdasOcupadas.length === 0) {
                semana.style.display = 'none';
            } else {
                semana.style.display = 'block';
            }
        });
    });
}

// Función para cambiar entre salas
document.addEventListener('DOMContentLoaded', function() {
    const botones = document.querySelectorAll('.agenda-btn');
    const salas = document.querySelectorAll('.agenda-sala');
    
    botones.forEach(boton => {
        boton.addEventListener('click', function() {
            const salaId = this.getAttribute('data-id');
            
            // Ocultar todas las salas
            salas.forEach(sala => {
                sala.style.display = 'none';
            });
            
            // Mostrar la sala seleccionada
            document.getElementById(salaId).style.display = 'block';
            
            // Actualizar estado de los botones
            botones.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
        });
    });
    
    // Activar el primer botón por defecto
    if (botones.length > 0) {
        botones[0].click();
    }
});
