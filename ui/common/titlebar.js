// ============================================================================
// TITLE BAR
// Shared by every page. Include it as the first element in <body>, without
// defer, so the bar is in place before the page first paints:
//   <script src="/common/titlebar.js"></script>
// The script tag is replaced with the title bar.
// ============================================================================

(function () {
    const SVG_NS = 'http://www.w3.org/2000/svg';
    const HOME_ICON_PATH = 'M3 11.5 12 4l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z';

    function buildHomeIcon() {
        const svg = document.createElementNS(SVG_NS, 'svg');
        svg.setAttribute('viewBox', '0 0 24 24');
        svg.setAttribute('width', '24');
        svg.setAttribute('height', '24');
        svg.setAttribute('aria-hidden', 'true');

        const path = document.createElementNS(SVG_NS, 'path');
        path.setAttribute('d', HOME_ICON_PATH);
        path.setAttribute('fill', 'currentColor');
        svg.appendChild(path);
        return svg;
    }

    function buildHomeLink() {
        const link = document.createElement('a');
        link.className = 'titlebar-home';
        link.href = '/';
        link.setAttribute('aria-label', 'Home');
        link.appendChild(buildHomeIcon());
        return link;
    }

    // Will be filled from the signed-in user later.
    function buildGreeting() {
        const name = document.createElement('span');
        name.id = 'user-name';
        name.textContent = 'Guest';

        const greeting = document.createElement('span');
        greeting.className = 'titlebar-user';
        greeting.append('Welcome ', name);
        return greeting;
    }

    const header = document.createElement('header');
    header.className = 'titlebar';
    header.append(buildHomeLink(), buildGreeting());

    document.currentScript.replaceWith(header);
})();
