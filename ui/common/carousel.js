// ============================================================================
// CAROUSEL
// Sponsor carousel built from DEFAULT_CAROUSEL (common/config.js), cross-fading
// through the items. Include it, without defer, where the carousel should
// appear:
//   <script src="/common/carousel.js"></script>
// The script tag is replaced with the carousel. Load common/config.js in
// <head>, without defer, before it.
// ============================================================================

(function () {
    const script = document.currentScript;
    const config = typeof DEFAULT_CAROUSEL !== 'undefined' ? DEFAULT_CAROUSEL : null;
    if (!config || !config.items || config.items.length === 0) {
        script.remove();
        return;
    }

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

    function buildCarousel(slides) {
        const track = document.createElement('div');
        track.className = 'carousel-track';
        track.append.apply(track, slides);

        const carousel = document.createElement('section');
        carousel.className = 'carousel';
        carousel.setAttribute('aria-label', 'Sponsors');
        carousel.setAttribute('aria-roledescription', 'carousel');
        carousel.appendChild(track);
        return carousel;
    }

    function rotate(carousel, slides) {
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
    }

    const slides = config.items.map(buildSlide);
    const carousel = buildCarousel(slides);
    script.replaceWith(carousel);
    if (slides.length > 1) rotate(carousel, slides);
})();
