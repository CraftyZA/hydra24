// ============================================================================
// TITLE BAR
// Shared by every page. Include it as the first element in <body>, without
// defer, so the bar is in place before the page first paints:
//   <script src="/common/titlebar.js"></script>
// The script tag is replaced with the title bar. Needs common/icon.js and
// common/auth.js.
// ============================================================================

(function () {
    const HOME_ICON_PATH = 'M3 11.5 12 4l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z';
    const CHEVRON_ICON_PATH = 'M7 10l5 5 5-5z';
    const ACCOUNT_MENU_ID = 'account-menu';

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

    function buildMenuItem(item) {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'account-menu-item';
        button.textContent = item.label;
        button.addEventListener('click', item.select);
        return button;
    }

    // "Welcome <email> ▾", opening a menu of items ({label, select}) that is
    // anchored under the title bar. Closes on Escape, outside tap or focus leaving.
    function buildAccountMenu(email, items) {
        const toggle = document.createElement('button');
        toggle.type = 'button';
        toggle.className = 'account-toggle';
        toggle.setAttribute('aria-expanded', 'false');
        toggle.setAttribute('aria-controls', ACCOUNT_MENU_ID);

        const label = document.createElement('span');
        label.className = 'account-toggle-label';
        label.append('Welcome ', email);
        toggle.append(label, createIcon(CHEVRON_ICON_PATH));

        const menu = document.createElement('div');
        menu.className = 'account-menu';
        menu.id = ACCOUNT_MENU_ID;
        menu.hidden = true;
        menu.append.apply(menu, items.map(buildMenuItem));

        const wrapper = document.createElement('div');
        wrapper.className = 'account';
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

    // The account menu when signed in, otherwise "Register / Login".
    function buildUserArea() {
        const area = document.createElement('div');
        area.className = 'titlebar-user';
        Auth.signedInEmail()
            .catch(function () { return null; })
            .then(function (email) {
                if (email) {
                    area.append(buildAccountMenu(email, [
                        {label: 'Sign out', select: Auth.signOut}
                    ]));
                } else {
                    area.append(buildAuthButton('Register', Auth.register), ' / ', buildAuthButton('Login', Auth.login));
                }
            });
        return area;
    }

    const header = document.createElement('header');
    header.className = 'titlebar';
    header.append(buildHomeLink(), buildUserArea());

    document.currentScript.replaceWith(header);
})();
