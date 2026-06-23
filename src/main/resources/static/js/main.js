// Carrusel del hero: rota entre las imágenes de fondo automáticamente
document.addEventListener('DOMContentLoaded', function () {
  const slides = document.querySelectorAll('.hero__slide');
  const dots   = document.querySelectorAll('.hero__dot');
  let current  = 0;
  let timer    = null;

  function show(index) {
    slides.forEach((s, i) => s.classList.toggle('is-active', i === index));
    dots.forEach((d, i) => d.classList.toggle('is-active', i === index));
    current = index;
  }

  function next() {
    show((current + 1) % slides.length);
  }

  function start() {
    timer = setInterval(next, 6000);
  }

  function reset() {
    clearInterval(timer);
    start();
  }

  // Click en los puntos
  dots.forEach((dot, i) => {
    dot.addEventListener('click', () => {
      show(i);
      reset();
    });
  });

  if (slides.length > 1) start();
});
