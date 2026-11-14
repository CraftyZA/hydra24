// ============================================================================
// TITLE BAR
// Shared by every page. Include it as the first element in <body>, without
// defer, so the bar is in place before the page first paints:
//   <script src="/common/titlebar.js"></script>
// The script tag is replaced with the title bar. Needs common/icon.js and
// common/auth.js.
// Admins also get a burger menu of the admin pages, left of the home button.
// ============================================================================

(function () {
    const HOME_ICON_PATH = 'M3 11.5 12 4l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z';
    const CHEVRON_ICON_PATH = 'M7 10l5 5 5-5z';
    const BURGER_ICON_PATH = 'M3 6h18v2H3zm0 5h18v2H3zm0 5h18v2H3z';
    const CLOSE_ICON_PATH = 'M6.4 5 12 10.6 17.6 5 19 6.4 13.4 12 19 17.6 17.6 19 12 13.4 6.4 19 5 17.6 10.6 12 5 6.4z';
    const ADMIN_PAGES = [
        {label: 'Members', href: '/admin/members/members.html'},
        {label: 'Events', href: '/admin/events/events.html'},
        {label: 'Classes', href: '/admin/classes/classes.html'}
    ];

    function buildHomeLink() {
        const link = document.createElement('a');
        link.className = 'titlebar-home';
        link.href = '/';
        link.setAttribute('aria-label', 'Home');
        link.appendChild(createIcon(HOME_ICON_PATH));
        return link;
    }

    function buildAuthButton(label, onClick) {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'titlebar-link';
        button.textContent = label;
        button.addEventListener('click', onClick);
        return button;
    }

    // A link for {label, href}, otherwise a button for {label, select}.
    function buildMenuItem(item) {
        const element = document.createElement(item.href ? 'a' : 'button');
        element.className = 'titlebar-menu-item';
        element.textContent = item.label;
        if (item.href) {
            element.href = item.href;
        } else {
            element.type = 'button';
            element.addEventListener('click', item.select);
        }
        return element;
    }

    // toggle opens a menu of items anchored under the title bar. Closes on Escape,
    // outside tap or focus leaving. menuClass places the menu.
    function buildDropdown(toggle, items, menuId, menuClass) {
        toggle.type = 'button';
        toggle.setAttribute('aria-expanded', 'false');
        toggle.setAttribute('aria-controls', menuId);

        const menu = document.createElement('div');
        menu.className = `titlebar-menu ${menuClass}`;
        menu.id = menuId;
        menu.hidden = true;
        menu.append.apply(menu, items.map(buildMenuItem));

        const wrapper = document.createElement('div');
        wrapper.className = 'titlebar-dropdown';
        wrapper.append(toggle, menu);

        function setOpen(open) {
            menu.hidden = !open;
            toggle.setAttribute('aria-expanded', String(open));
        }

        toggle.addEventListener('click', function () { setOpen(menu.hidden); });
        wrapper.addEventListener('keydown', function (event) {
            if (event.key === 'Escape' && !menu.hidden) {
                setOpen(false);
                toggle.focus();
            }
        });
        wrapper.addEventListener('focusout', function (event) {
            if (!wrapper.contains(event.relatedTarget)) setOpen(false);
        });
        document.addEventListener('click', function (event) {
            if (!wrapper.contains(event.target)) setOpen(false);
        });
        return wrapper;
    }

    // Three bars, turning into a cross while the menu is open.
    function buildAdminMenu() {
        const toggle = document.createElement('button');
        toggle.className = 'burger-toggle';
        toggle.setAttribute('aria-label', 'Admin menu');
        const open = createIcon(BURGER_ICON_PATH);
        open.classList.add('burger-open-icon');
        const close = createIcon(CLOSE_ICON_PATH);
        close.classList.add('burger-close-icon');
        toggle.append(open, close);
        return buildDropdown(toggle, ADMIN_PAGES, 'admin-menu', 'admin-menu');
    }

    // "Welcome <email> ▾" with Sign out.
    function buildAccountMenu(email) {
        const toggle = document.createElement('button');
        toggle.className = 'account-toggle';

        const label = document.createElement('span');
        label.className = 'account-toggle-label';
        label.append('Welcome ', email);
        toggle.append(label, createIcon(CHEVRON_ICON_PATH));

        return buildDropdown(toggle, [{label: 'Sign out', select: Auth.signOut}], 'account-menu', 'account-menu');
    }

    function buildSignedOutLinks() {
        return [buildAuthButton('Register', Auth.register), ' / ', buildAuthButton('Login', Auth.login)];
    }

    const start = document.createElement('div');
    start.className = 'titlebar-start';
    start.append(buildHomeLink());

    const user = document.createElement('div');
    user.className = 'titlebar-user';

    Promise.all([Auth.signedInEmail(), Auth.isAdmin()])
        .catch(function () { return [null, false]; })
        .then(function ([email, isAdmin]) {
            if (isAdmin) start.prepend(buildAdminMenu());
            user.append(...(email ? [buildAccountMenu(email)] : buildSignedOutLinks()));
        });

    const header = document.createElement('header');
    header.className = 'titlebar';
    header.append(start, user);

    document.currentScript.replaceWith(header);
})();
