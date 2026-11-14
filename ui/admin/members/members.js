(function () {
    const search = document.getElementById('search');

    function renderMember(member) {
        const title = member.memberNo ? `#${member.memberNo} ${MemberText.name(member)}` : MemberText.name(member);
        return el('a', {class: 'row-card', href: `/admin/members/member.html?id=${member.userId}`},
            el('span', {class: 'row-title', text: title}),
            el('span', {class: 'row-detail', text: member.email}),
            el('span', {class: 'row-detail', text: member.cellphone || 'No profile yet'}));
    }

    function matches(member, text) {
        const searchable = [member.memberNo, member.firstName, member.lastName, member.email].join(' ').toLowerCase();
        return searchable.includes(text);
    }

    AdminPage.run(document.getElementById('status'), async function () {
        const list = createPagedList(document.getElementById('members'), renderMember, 'No members found.');
        const members = await Api.get('/api/admin/members');
        document.getElementById('heading').textContent = `Members (${members.length})`;

        function showMatching() {
            const text = search.value.trim().toLowerCase();
            list.show(members.filter(function (member) { return matches(member, text); }));
        }

        search.addEventListener('input', showMatching);
        showMatching();
    });
})();
