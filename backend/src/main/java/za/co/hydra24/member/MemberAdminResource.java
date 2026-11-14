package za.co.hydra24.member;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import za.co.hydra24.api.ApiErrors;
import za.co.hydra24.entry.EntryRepository;
import za.co.hydra24.entry.MyEntry;
import za.co.hydra24.profile.Profile;
import za.co.hydra24.profile.ProfileRepository;
import za.co.hydra24.security.AdminOnly;

import java.util.List;

/**
 * Registered users, their profiles and their entries, keyed on the user id. The
 * profile never carries the ID number, race or disability, in either direction.
 */
@Path("/api/admin/members")
@AdminOnly
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MemberAdminResource {

    @Inject
    MemberRepository members;

    @Inject
    ProfileRepository profiles;

    @Inject
    EntryRepository entries;

    @GET
    public List<Member> list() {
        return members.listAll();
    }

    @GET
    @Path("/{userId}")
    public Member get(@PathParam("userId") long userId) {
        return members.find(userId).orElseThrow(NotFoundException::new);
    }

    /** 204 while the member has not set up a profile. */
    @GET
    @Path("/{userId}/profile")
    public Profile getProfile(@PathParam("userId") long userId) {
        get(userId);
        return profiles.findByUserId(userId).map(Profile::withoutSpecialInformation).orElse(null);
    }

    @PUT
    @Path("/{userId}/profile")
    public void updateProfile(@PathParam("userId") long userId, @Valid Profile profile) {
        get(userId);
        if (!profiles.updateByUserId(userId, profile)) {
            throw ApiErrors.conflict("This member has not set up a profile yet.");
        }
    }

    @GET
    @Path("/{userId}/entries")
    public List<MyEntry> listEntries(@PathParam("userId") long userId) {
        Long memberNo = get(userId).memberNo();
        return memberNo == null ? List.of() : entries.listForProfile(memberNo);
    }
}
