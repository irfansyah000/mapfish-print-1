package org.mapfish.print.config.access;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mapfish.print.config.access.AccessAssertionTestUtil.setCreds;

public class RoleAccessAssertionTest {

    @After
    public void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
    }

    @Test(expected = AssertionError.class)
    public void testSetRequiredRoles() throws Exception {
        final RoleAccessAssertion assertion = new RoleAccessAssertion();
        assertion.setRequiredRoles(Sets.newHashSet("ROLE_USER"));
        assertion.setRequiredRoles(Sets.newHashSet("ROLE_USER"));
    }

    @Test (expected = AuthenticationCredentialsNotFoundException.class)
    public void testAssertAccessNoCredentials() throws Exception {
        final RoleAccessAssertion assertion = new RoleAccessAssertion();
        assertion.setRequiredRoles(Sets.newHashSet("ROLE_USER"));

        assertion.assertAccess("", this);
    }

    @Test (expected = AccessDeniedException.class)
    public void testAssertAccessWrongCreds() throws Exception {
        final RoleAccessAssertion assertion = new RoleAccessAssertion();
        assertion.setRequiredRoles(Sets.newHashSet("ROLE_USER"));

        setCreds("ROLE_USER2");
        assertion.assertAccess("", this);
    }

    @Test
    public void testAssertAccessAllowed() throws Exception {
        final RoleAccessAssertion assertion = new RoleAccessAssertion();
        assertion.setRequiredRoles(Sets.newHashSet("ROLE_USER"));

        setCreds("ROLE_USER");
        assertion.assertAccess("", this);

        setCreds("ROLE_USER", "ROLE_OTHER");
        assertion.assertAccess("", this);
    }
    @Test
    public void testAssertAccessOneOf() throws Exception {
        final RoleAccessAssertion assertion = new RoleAccessAssertion();
        assertion.setRequiredRoles(Sets.newHashSet("ROLE_USER", "ROLE_USER2"));

        setCreds("ROLE_USER");
        assertion.assertAccess("", this);

        setCreds("ROLE_USER2");
        assertion.assertAccess("", this);

        setCreds("ROLE_OTHER", "ROLE_USER2");
        assertion.assertAccess("", this);

    }

    @Test (expected = AccessDeniedException.class)
    public void testAssertAccessOneOfFailed() throws Exception {
        final RoleAccessAssertion assertion = new RoleAccessAssertion();
        assertion.setRequiredRoles(Sets.newHashSet("ROLE_USER", "ROLE_USER2"));

        setCreds("ROLE_OTHER");
        assertion.assertAccess("", this);

    }

    @Test (expected = AuthenticationCredentialsNotFoundException.class)
    public void testAssertNoRolesNoCreds() throws Exception {
        final RoleAccessAssertion assertion = new RoleAccessAssertion();
        assertion.setRequiredRoles(Sets.<String>newHashSet());

        assertion.assertAccess("", this);
        setCreds("ROLE_OTHER", "ROLE_USER2");
        assertion.assertAccess("", this);
    }

    @Test
    public void testAssertNoRolesSomeCreds() throws Exception {
        final RoleAccessAssertion assertion = new RoleAccessAssertion();
        assertion.setRequiredRoles(Sets.<String>newHashSet());

        setCreds("ROLE_OTHER");
        assertion.assertAccess("", this);

        setCreds("ROLE_USER");
        assertion.assertAccess("", this);
    }

    @Test (expected = AuthenticationCredentialsNotFoundException.class)
    public void testMarshalUnmarshalNoAuth() throws Exception {
        final RoleAccessAssertion assertion = new RoleAccessAssertion();
        assertion.setRequiredRoles(Sets.newHashSet("ROLE_USER"));
        final JSONObject marshalData = assertion.marshal();

        RoleAccessAssertion newAssertion = new RoleAccessAssertion();
        newAssertion.unmarshal(marshalData);
        newAssertion.assertAccess("", this);
    }

    @Test (expected = AccessDeniedException.class)
    public void testMarshalUnmarshalNotPermitted() throws Exception {
        setCreds("ROLE_OTHER");
        final RoleAccessAssertion assertion = new RoleAccessAssertion();
        assertion.setRequiredRoles(Sets.newHashSet("ROLE_USER"));
        final JSONObject marshalData = assertion.marshal();

        RoleAccessAssertion newAssertion = new RoleAccessAssertion();
        newAssertion.unmarshal(marshalData);
        newAssertion.assertAccess("", this);
    }


    @Test
    public void testMarshalUnmarshalAllowed() throws Exception {
        setCreds("ROLE_USER");
        final RoleAccessAssertion assertion = new RoleAccessAssertion();
        assertion.setRequiredRoles(Sets.newHashSet("ROLE_USER"));
        final JSONObject marshalData = assertion.marshal();

        RoleAccessAssertion newAssertion = new RoleAccessAssertion();
        newAssertion.unmarshal(marshalData);
        newAssertion.assertAccess("", this);
    }


    @Test
    public void testValidate() throws Exception {
        List<Throwable> errors = Lists.newArrayList();
        final RoleAccessAssertion assertion = new RoleAccessAssertion();
        assertion.validate(errors, null);
        assertEquals(1, errors.size());
        errors.clear();
        assertion.setRequiredRoles(Sets.newHashSet("ROLE_USER"));
        assertion.validate(errors, null);
        assertEquals(0, errors.size());
    }
}
