/*
 * File:        cards.js
 * Module:      Public dashboard (S1)
 * Purpose:     Adds a Read more button to report and news cards whose description or location is cut off.
 * Responsible: Ythan Joshua Costelo
 */
document.querySelectorAll('.card, .hot-card').forEach(card => {
  const texts = card.querySelectorAll('.card-description, .card-location');
  const button = document.createElement('button');
  const isOpen = () => card.classList.contains('open');
  const label = () => {
    button.textContent = isOpen() ? 'Show less' : 'Read more';
    button.ariaLabel = `${button.textContent}: ${card.querySelector('.card-title').textContent}`;
    button.ariaExpanded = isOpen();
  };
  const check = () => {
    button.hidden = !isOpen() && [...texts].every(text => text.scrollHeight <= text.clientHeight + 1);
  };

  button.type = 'button';
  button.className = 'read-more';
  button.addEventListener('click', () => {
    card.classList.toggle('open');
    label();
    if (!isOpen()) {
      card.scrollIntoView({block: 'nearest'});
    }
  });
  label();
  texts[0].after(button);
  const observer = new ResizeObserver(check);
  texts.forEach(text => observer.observe(text));
  // A web font that arrives late can change the number of lines without changing the height.
  document.fonts.ready.then(check);
});
