// ============================================================================
// BACK BUTTON
// Goes to the parent page, not browser history. The parent comes from the
// folder layout:
//   /events/events.html   -> /                    (section page)
//   /events/calendar.html -> /events/events.html  (sub page)
// Include it, without defer, where the button should appear:
//   <script src="/common/backbutton.js"></script>
// The script tag is replaced with the button. Needs common/icon.js.
// ============================================================================

(function () {
    const BACK_ICON_PATH = 'M15.4 4.6 8 12l7.4 7.4-1.4 1.4L5.2 12 14 3.2z';

    function parentPath(pathname) {
        const parts = pathname.split('/').filter(Boolean);
        if (parts.length < 2) return '/';

        const folder = parts[parts.length - 2];
        const page = parts[parts.length - 1];
        const sectionPage = folder + '.html';
        if (page === sectionPage) return '/';
        return '/' + parts.slice(0, -1).join('/') + '/' + sectionPage;
    }

    const link = document.createElement('a');
    link.className = 'back-button';
    link.href = parentPath(window.location.pathname);
    link.setAttribute('aria-label', 'Back');
    link.appendChild(createIcon(BACK_ICON_PATH));

    document.currentScript.replaceWith(link);
})();
