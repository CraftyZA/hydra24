// ============================================================================
// BACK BUTTON
// Goes to the parent page, not browser history. The parent comes from the
// folder layout:
//   /events/events.html            -> /                             (section page)
//   /events/rules.html             -> /events/events.html           (sub page)
//   /events/calendar/calendar.html -> /events/events.html           (nested section)
//   /events/calendar/event.html    -> /events/calendar/calendar.html
// Include it, without defer, where the button should appear:
//   <script src="/common/backbutton.js"></script>
// The script tag is replaced with the button. Needs common/icon.js.
// ============================================================================

(function () {
    const BACK_ICON_PATH = 'M15.4 4.6 8 12l7.4 7.4-1.4 1.4L5.2 12 14 3.2z';

    function parentPath(pathname) {
        const folders = pathname.split('/').filter(Boolean);
        const page = folders.pop();
        const isSectionPage = page === folders[folders.length - 1] + '.html';
        if (isSectionPage) folders.pop();
        if (folders.length === 0) return '/';
        return '/' + folders.join('/') + '/' + folders[folders.length - 1] + '.html';
    }

    const link = document.createElement('a');
    link.className = 'back-button';
    link.href = parentPath(window.location.pathname);
    link.setAttribute('aria-label', 'Back');
    link.appendChild(createIcon(BACK_ICON_PATH));

    document.currentScript.replaceWith(link);
})();
