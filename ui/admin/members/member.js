(function () {
    const status = document.getElementById('status');
    const form = document.getElementById('profile-form');
    const id = encodeURIComponent(AdminPage.queryId());
    const API_PATH = `/api/admin/members/${id}`;

    function showMember(member) {
        document.getElementById('heading').textContent = MemberText.name(member);
        document.getElementById('account').textContent = [
            member.email,
            member.memberNo ? `Member no. ${member.memberNo}` : 'No profile yet',
            `Last login ${Format.dateTime(member.lastLoginAt)}`
        ].join(' - ');
        if (member.memberNo) {
            const entriesLink = document.getElementById('entries-link');
            entriesLink.href = `/admin/members/entries.html?id=${id}`;
            entriesLink.hidden = false;
        }
    }

    AdminPage.run(status, async function () {
        const [member, profile, options, classes] = await Promise.all([
            Api.get(API_PATH),
            Api.get(`${API_PATH}/profile`),
            Api.getPublic('/api/options'),
            Api.getPublic('/api/classes')
        ]);
        showMember(member);
        if (!profile) return;

        ProfileForm.build(form, options, classes);
        Form.write(form, profile);
        Form.steps(form);
        ProfileForm.saveOnSubmit(form, status, `${API_PATH}/profile`);
        form.hidden = false;
    });
})();
