let lista = document.getElementById("lista-tornei");

for (let i = 0; i < tornei.length; i++) {
  lista.innerHTML += "<div class='card'>"
    + "<h3>" + tornei[i].nome + "</h3>"
    + "<p>Stato: " + tornei[i].stato + "</p>"
    + "<p>Tipo: " + tornei[i].tipo + "</p>"
    + "<p>Iscrizioni fino al: " + tornei[i].fineIscrizioni + "</p>"
    + "<p>Iscritti: " + tornei[i].iscritti + "</p>"
    + "<a href='torneo.html?id=" + tornei[i].id + "'>Vedi dettagli</a>"
    + "</div>";
}