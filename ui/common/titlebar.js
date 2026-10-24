// ============================================================================
// TITLE BAR
// Shared by every page. Include it as the first element in <body>, without
// defer, so the bar is in place before the page first paints:
//   <script src="/common/titlebar.js"></script>
// The script tag is replaced with the title bar. Needs common/icon.js.
// ============================================================================

(function () {
    const HOME_ICON_PATH = 'M3 11.5 12 4l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z';

    function buildHomeLink() {
        const link = document.createElement('a');
        link.className = 'titlebar-home';
        link.href = '/';
        link.setAttribute('aria-label', 'Home');
        link.appendChild(createIcon(HOME_ICON_PATH));
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
