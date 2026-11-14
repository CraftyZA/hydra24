// ============================================================================
// MEMBER TEXT
// How a member (/api/admin/members) reads on the admin member pages.
//   MemberText.name(member)    their name, or their email before they set up a profile
// ============================================================================

const MemberText = {
    name: function (member) {
        return member.memberNo ? `${member.firstName} ${member.lastName}` : member.email;
    }
};
