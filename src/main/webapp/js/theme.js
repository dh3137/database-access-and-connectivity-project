(function () {
  const KEY = 'veloce-theme';

  function applyTheme(theme) {
    if (theme === 'light') {
      document.documentElement.setAttribute('data-theme', 'light');
    } else {
      document.documentElement.removeAttribute('data-theme');
    }
    document.querySelectorAll('.v-theme-toggle').forEach(btn => {
      btn.setAttribute('aria-label', theme === 'light' ? 'Switch to dark mode' : 'Switch to light mode');
      btn.textContent = theme === 'light' ? '☽' : '☀';
    });
  }

  function savedTheme() {
    const stored = localStorage.getItem(KEY);
    if (stored) return stored;
    return window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
  }

  window.toggleTheme = function () {
    const next = document.documentElement.getAttribute('data-theme') === 'light' ? 'dark' : 'light';
    localStorage.setItem(KEY, next);
    applyTheme(next);
  };

  applyTheme(savedTheme());

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => applyTheme(savedTheme()));
  }
})();
