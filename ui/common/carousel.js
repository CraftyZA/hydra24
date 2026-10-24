// ============================================================================
// CAROUSEL
// Fills every .carousel on the page from DEFAULT_CAROUSEL (common/config.js)
// and cross-fades through the items. The page ships with the first slide in
// its HTML, so it still shows something if this script never runs.
// ============================================================================

(function () {
    const config = typeof DEFAULT_CAROUSEL !== 'undefined' ? DEFAULT_CAROUSEL : null;
    if (!config || !config.items || config.items.length === 0) return;

    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    function buildSlide(item, index) {
        const img = document.createElement('img');
        img.src = item.image;
        img.alt = item.alt || '';
        img.decoding = 'async';

        let slide = img;
        if (item.link) {
            slide = document.createElement('a');
            slide.href = item.link;
            slide.target = '_blank';
            slide.rel = 'noopener noreferrer';
            slide.appendChild(img);
        }
        slide.classList.add('carousel-slide');
        if (index === 0) slide.classList.add('is-active');
        else slide.setAttribute('aria-hidden', 'true');
        return slide;
    }

    document.querySelectorAll('.carousel').forEach(function (carousel) {
        const track = carousel.querySelector('.carousel-track');
        if (!track) return;

        const slides = config.items.map(buildSlide);
        track.replaceChildren.apply(track, slides);
        if (slides.length < 2) return;

        let current = 0;
        let timer = null;

        function show(next) {
            slides[current].classList.remove('is-active');
            slides[current].setAttribute('aria-hidden', 'true');
            current = next;
            slides[current].classList.add('is-active');
            slides[current].removeAttribute('aria-hidden');
        }

        function start() {
            if (timer || reduceMotion) return;
            timer = setInterval(function () {
                show((current + 1) % slides.length);
            }, config.duration || 3000);
        }

        function stop() {
            clearInterval(timer);
            timer = null;
        }

        // Don't rotate while hidden or while someone is hovering/focused on it.
        document.addEventListener('visibilitychange', function () {
            if (document.hidden) stop(); else start();
        });
        carousel.addEventListener('mouseenter', stop);
        carousel.addEventListener('mouseleave', start);
        carousel.addEventListener('focusin', stop);
        carousel.addEventListener('focusout', start);

        start();
    });
})();
