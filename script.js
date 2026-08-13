let scripts = [];

document.addEventListener('DOMContentLoaded', async () => {
    try {
        const res = await fetch('data.json');
        scripts = await res.json();
        renderScripts();
    } catch (e) {
        console.error('Error:', e);
        document.getElementById('scripts-grid').innerHTML = '<p>Error cargando scripts</p>';
    }
});

function renderScripts() {
    const grid = document.getElementById('scripts-grid');
    grid.innerHTML = scripts.map(s => `
        <div class="script-card">
            <div class="script-card-header">
                <div class="script-card-title">${s.nombre}</div>
            </div>
            <div class="script-card-body">
                <p>${s.desc}</p>
                <p style="color: #999; font-size: 0.9rem; margin-top: 0.5rem;">📄 ${s.archivo}</p>
            </div>
            <div class="script-card-footer">
                <a href="https://github.com/mijolandia/Frio-extremo/raw/main/${s.archivo}" class="btn-descargar" download>
                    ⬇️ Descargar
                </a>
            </div>
        </div>
    `).join('');
}
