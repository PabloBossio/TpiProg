const listaSubastas = document.getElementById("listaSubastas");
const formSubasta = document.getElementById("formSubasta");

let subastas = [
{
id: 1,
titulo: "Notebook Lenovo",
descripcion: "Notebook usada en buen estado",
precioBase: 150000
},
{
id: 2,
titulo: "Celular Samsung",
descripcion: "Celular con cargador incluido",
precioBase: 80000
}
];

function mostrarSubastas() {
listaSubastas.innerHTML = "";

if (subastas.length === 0) {
listaSubastas.innerHTML = "<p>No hay subastas cargadas.</p>";
return;
}

subastas.forEach(function(subasta) {
const tarjeta = document.createElement("div");
tarjeta.className = "subasta";

tarjeta.innerHTML = `
<h3>${subasta.titulo}</h3>
<p>${subasta.descripcion}</p>
<p class="precio">Precio base: $${subasta.precioBase}</p>
<button onclick="ofertar(${subasta.id})">Ofertar</button>
`;

listaSubastas.appendChild(tarjeta);
});
}

function ofertar(id) {
const monto = prompt("Ingrese el monto de la oferta:");

if (monto === null || monto.trim() === "") {
return;
}

alert("Oferta registrada para la subasta " + id + ": $" + monto);
}

formSubasta.addEventListener("submit", function(evento) {
evento.preventDefault();

const nuevaSubasta = {
id: subastas.length + 1,
titulo: document.getElementById("titulo").value,
descripcion: document.getElementById("descripcion").value,
precioBase: Number(document.getElementById("precioBase").value)
};

subastas.push(nuevaSubasta);

formSubasta.reset();
mostrarSubastas();
});

mostrarSubastas();