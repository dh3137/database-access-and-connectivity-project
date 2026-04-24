/**
 * Scroll-reveal — wires IntersectionObserver to [data-reveal] elements.
 * Add data-reveal="up|down|left|right|scale|fade" to any element.
 * Optionally set style="--reveal-delay:100ms" for stagger effects.
 * Elements re-animate every time they scroll into view.
 */
(function () {
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('revealed');
        } else {
          // Reset so it re-animates next time it enters
          entry.target.classList.remove('revealed');
        }
      });
    },
    { threshold: 0.1, rootMargin: '0px 0px -48px 0px' }
  );

  function observe() {
    document.querySelectorAll('[data-reveal]').forEach((el) => observer.observe(el));
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', observe);
  } else {
    observe();
  }

  window.revealRefresh = observe;
})();
